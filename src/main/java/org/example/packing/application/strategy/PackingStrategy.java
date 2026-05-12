package org.example.packing.application.strategy;

import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicle;
import org.example.packing.domain.model.Vehicle;

import java.util.List;

/**
 * Strategy for packing a list of packages into one or more vehicles.
 * <p>
 * Implementations must:
 * <ul>
 *   <li>Never violate vehicle constraints (weight, volume, no overlaps)</li>
 *   <li>Be deterministic for a given input (for testability)</li>
 *   <li>Not mutate input collections</li>
 * </ul>
 */
public interface PackingStrategy {

	/**
	 * Packs the given packages into the given vehicles.
	 *
	 * @param packages packages to pack (input is not mutated)
	 * @param vehicles available vehicles, ordered by preference
	 *
	 * @return result containing packed vehicles and any unpacked packages
	 */
	PackingResult pack(List<Package> packages, List<Vehicle> vehicles);

	/**
	 * Human-readable name of the strategy.
	 */
	String name();

	/**
	 * Result of a packing operation.
	 */
	record PackingResult(List<PackedVehicle> packedVehicles, List<Package> unpackedPackages) {
		public PackingResult {
			packedVehicles = List.copyOf(packedVehicles);
			unpackedPackages = List.copyOf(unpackedPackages);
		}

		public boolean isFullyPacked() {
			return unpackedPackages.isEmpty();
		}
	}
}
