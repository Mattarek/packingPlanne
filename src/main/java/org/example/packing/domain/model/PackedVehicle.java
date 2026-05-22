package org.example.packing.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root representing a vehicle being loaded with packages.
 * <p>
 * Enforces invariants:
 * <ul>
 *   <li>Total weight of placed packages never exceeds {@code vehicle.maxPayload()}</li>
 *   <li>No package extends beyond the cargo area boundaries</li>
 *   <li>No two placed packages overlap in 3D space</li>
 * </ul>
 * <p>
 * This class is mutable but always consistent — every successful {@link #place} call
 * leaves the aggregate in a valid state.
 */
public final class PackedVehicle {

	private final Vehicle vehicle;
	private final List<PlacedPackage> placedPackages;
	private Weight currentWeight;

	public PackedVehicle(final Vehicle vehicle) {
		this.vehicle = Objects.requireNonNull(vehicle, "vehicle must not be null");
		placedPackages = new ArrayList<>();
		currentWeight = Weight.ZERO;
	}

	public Vehicle vehicle() {
		return vehicle;
	}

	public List<PlacedPackage> placedPackages() {
		return Collections.unmodifiableList(placedPackages);
	}

	public Weight currentWeight() {
		return currentWeight;
	}

	public Weight remainingPayload() {
		return new Weight(vehicle.maxPayload().kilograms() - currentWeight.kilograms());
	}

	public double currentVolume() {
		return placedPackages.stream()
				.mapToDouble(p -> p.pkg().volume())
				.sum();
	}

	/**
	 * Volume utilization ratio in range [0.0, 1.0].
	 */
	public double volumeUtilization() {
		final double cargo = vehicle.cargoVolume();
		return cargo == 0 ? 0.0 : currentVolume() / cargo;
	}

	/**
	 * Weight utilization ratio in range [0.0, 1.0].
	 */
	public double weightUtilization() {
		final double max = vehicle.maxPayload().kilograms();
		return max == 0 ? 0.0 : currentWeight.kilograms() / max;
	}

	/**
	 * Tests whether the given package can be placed at the given position
	 * without violating any invariant. Pure query — does not mutate state.
	 */
	public boolean canPlace(final Package pkg, final Position position) {
		Objects.requireNonNull(pkg, "pkg must not be null");
		Objects.requireNonNull(position, "position must not be null");

		if (!currentWeight.add(pkg.weight()).isLessThanOrEqualTo(vehicle.maxPayload())) {
			return false;
		}

		final PlacedPackage candidate = new PlacedPackage(pkg, position);

		if (!candidate.fitsInside(vehicle.cargoArea())) {
			return false;
		}

		for (final PlacedPackage existing : placedPackages) {
			if (candidate.overlapsWith(existing)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Places a package at the given position.
	 *
	 * @throws IllegalStateException if placement would violate any invariant
	 */
	public void place(final Package pkg, final Position position) {
		if (!canPlace(pkg, position)) {
			throw new IllegalStateException(
					"Cannot place package %s at %s — would violate constraints"
							.formatted(pkg.id(), position));
		}
		placedPackages.add(new PlacedPackage(pkg, position));
		currentWeight = currentWeight.add(pkg.weight());
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public List<PlacedPackage> getPlacedPackages() {
		return Collections.unmodifiableList(placedPackages);
	}

	public Weight getCurrentWeight() {
		return currentWeight;
	}

	public double getCurrentVolume() {
		return currentVolume();
	}

	public double getVolumeUtilization() {
		return volumeUtilization();
	}

	public double getWeightUtilization() {
		return weightUtilization();
	}

	@Override
	public String toString() {
		return "PackedVehicle[%s, packed=%d, weight=%s/%s, vol=%.1f%%]"
				.formatted(
						vehicle.id(),
						placedPackages.size(),
						currentWeight,
						vehicle.maxPayload(),
						volumeUtilization() * 100);
	}
}
