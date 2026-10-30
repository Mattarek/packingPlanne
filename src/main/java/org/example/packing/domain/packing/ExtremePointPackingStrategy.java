package org.example.packing.domain.packing;

import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicle;
import org.example.packing.domain.model.Position;
import org.example.packing.domain.model.Vehicle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ExtremePointPackingStrategy implements PackingStrategy {

	private final PackageOrdering ordering;
	private final CandidatePointGenerator pointGenerator;
	private final Comparator<Position> positionPreference;

	public ExtremePointPackingStrategy() {
		this(PackageOrdering.VOLUME_DESC);
	}

	public ExtremePointPackingStrategy(final PackageOrdering ordering) {
		this(ordering, PositionPreferences.BOTTOM_LEFT_BACK);
	}

	public ExtremePointPackingStrategy(final PackageOrdering ordering, final Comparator<Position> positionPreference) {
		this(ordering, positionPreference, new ExtremePointGenerator());
	}

	public ExtremePointPackingStrategy(
			final PackageOrdering ordering,
			final Comparator<Position> positionPreference,
			final CandidatePointGenerator pointGenerator
	) {
		this.ordering = Objects.requireNonNull(ordering, "ordering must not be null");
		this.positionPreference = Objects.requireNonNull(positionPreference, "positionPreference must not be null");
		this.pointGenerator = Objects.requireNonNull(pointGenerator, "pointGenerator must not be null");
	}

	@Override
	public PackingResult pack(final List<Package> packages, final List<Vehicle> vehicles) {
		Objects.requireNonNull(packages, "packages must not be null");
		Objects.requireNonNull(vehicles, "vehicles must not be null");

		final List<Package> sorted = sortPackages(packages);
		final List<PackedVehicle> packedVehicles = new ArrayList<>();
		final List<Package> unpacked = new ArrayList<>();

		final VehicleAllocator allocator = new VehicleAllocator(vehicles);

		for (final Package pkg : sorted) {
			if (!tryPlaceInExistingVehicles(pkg, packedVehicles)
					&& !tryOpenNewVehicleAndPlace(pkg, allocator, packedVehicles)) {
				unpacked.add(pkg);
			}
		}
		return new PackingResult(
				packedVehicles.stream().map(PackedVehicle::snapshot).toList(),
				unpacked
		);
	}

	@Override
	public String name() {
		return "ExtremePoint(" + ordering.name() + ")";
	}

	private List<Package> sortPackages(final List<Package> packages) {
		final List<Package> sorted = new ArrayList<>(packages);
		sorted.sort(ordering.comparator());
		return sorted;
	}

	private boolean tryPlaceInExistingVehicles(final Package pkg, final List<PackedVehicle> packed) {
		for (final PackedVehicle vehicle : packed) {
			if (tryPlace(pkg, vehicle)) {
				return true;
			}
		}
		return false;
	}

	private boolean tryOpenNewVehicleAndPlace(
			final Package pkg, final VehicleAllocator allocator, final List<PackedVehicle> packed) {
		while (allocator.hasNext()) {
			final PackedVehicle next = allocator.openNext();
			packed.add(next);
			if (tryPlace(pkg, next)) {
				return true;
			}
		}
		return false;
	}

	private boolean tryPlace(final Package pkg, final PackedVehicle vehicle) {
		final Set<Position> candidates = candidatePositions(vehicle);

		final Position best = candidates.stream()
				.filter(pos -> vehicle.canPlace(pkg, pos))
				.min(positionPreference)
				.orElse(null);

		if (best == null) {
			return false;
		}
		vehicle.place(pkg, best);
		return true;
	}

	private Set<Position> candidatePositions(final PackedVehicle vehicle) {
		final Set<Position> positions = new LinkedHashSet<>();
		positions.add(Position.ORIGIN);
		vehicle.placedPackages().forEach(p ->
				positions.addAll(pointGenerator.pointsAfterPlacing(p)));
		return positions;
	}

	private static final class VehicleAllocator {
		private final List<Vehicle> vehicles;
		private int nextIndex;

		VehicleAllocator(final List<Vehicle> vehicles) {
			this.vehicles = vehicles;
		}

		boolean hasNext() {
			return nextIndex < vehicles.size();
		}

		PackedVehicle openNext() {
			if (!hasNext()) {
				throw new IllegalStateException("No more vehicles available");
			}
			return new PackedVehicle(vehicles.get(nextIndex++));
		}
	}
}
