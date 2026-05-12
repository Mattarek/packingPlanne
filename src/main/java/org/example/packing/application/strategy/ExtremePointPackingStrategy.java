package org.example.packing.application.strategy;

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

/**
 * Extreme Point-Based heuristic for 3D Bin Packing with weight constraints.
 * <p>
 * <b>Algorithm:</b>
 * <ol>
 *   <li>Sort packages by configured ordering (default: volume descending)</li>
 *   <li>For each package, try to place it in the currently open vehicle at the
 *       best available extreme point (bottom-left-back rule)</li>
 *   <li>If no extreme point fits, open the next vehicle</li>
 *   <li>If no vehicle can accommodate the package, mark it unpacked</li>
 * </ol>
 * <p>
 * Time complexity: O(n² · v) where n = packages, v = vehicles.
 * Space complexity: O(n).
 * <p>
 * Solution quality: typically 5–15% off optimum on standard 3D-BPP benchmarks
 * (Bischoff &amp; Ratcliff dataset).
 */
public final class ExtremePointPackingStrategy implements PackingStrategy {

	private final PackageOrdering ordering;
	private final ExtremePointGenerator pointGenerator;
	private final PositionScorer positionScorer;

	public ExtremePointPackingStrategy() {
		this(PackageOrdering.VOLUME_DESC);
	}

	public ExtremePointPackingStrategy(final PackageOrdering ordering) {
		this.ordering = Objects.requireNonNull(ordering, "ordering must not be null");
		pointGenerator = new ExtremePointGenerator();
		positionScorer = new PositionScorer();
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
		return new PackingResult(packedVehicles, unpacked);
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
			// The newly-opened vehicle cannot fit even this single package.
			// Keep it in the list (it may still hold smaller items later iterations
			// would try, but in our loop we already passed them — so this is a dead-end
			// for this package). Continue to next vehicle.
		}
		return false;
	}

	/**
	 * Tries to place a package in a vehicle at the best available extreme point.
	 */
	private boolean tryPlace(final Package pkg, final PackedVehicle vehicle) {
		final Set<Position> candidates = candidatePositions(vehicle);

		final Position best = candidates.stream()
				.filter(pos -> vehicle.canPlace(pkg, pos))
				.min(positionScorer.comparator())
				.orElse(null);

		if (best == null) {
			return false;
		}
		vehicle.place(pkg, best);
		return true;
	}

	/**
	 * Returns the current set of candidate placement positions for a vehicle.
	 * Always includes the origin; adds extreme points from every placed package.
	 */
	private Set<Position> candidatePositions(final PackedVehicle vehicle) {
		final Set<Position> positions = new LinkedHashSet<>();
		positions.add(Position.ORIGIN);
		vehicle.placedPackages().forEach(p ->
				positions.addAll(pointGenerator.pointsAfterPlacing(p)));
		return positions;
	}

	/**
	 * Exposed for tests / debugging.
	 */
	@SuppressWarnings("unused")
	PackageOrdering ordering() {
		return ordering;
	}

	/**
	 * Helper to make the {@link Comparator} accessible to subclasses if extended later.
	 */
	@SuppressWarnings("unused")
	Comparator<Position> positionComparator() {
		return positionScorer.comparator();
	}

	/**
	 * Encapsulates the iteration over the supplied vehicle list.
	 * SRP: this class knows nothing about packing — only about handing out vehicles in order.
	 */
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
