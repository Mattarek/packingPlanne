package org.example.packing.domain.model;

import java.util.List;
import java.util.Objects;

public record PackingStatistics(
		int packedPackagesCount,
		int vehiclesUsed,
		boolean fullyPacked,
		double averageVolumeUtilization,
		double averageWeightUtilization
) {

	public static PackingStatistics of(
			final List<PackedVehicleSnapshot> packedVehicles,
			final List<Package> unpackedPackages
	) {
		Objects.requireNonNull(packedVehicles, "packedVehicles must not be null");
		Objects.requireNonNull(unpackedPackages, "unpackedPackages must not be null");

		final List<PackedVehicleSnapshot> usedVehicles = packedVehicles.stream()
				.filter(PackedVehicleSnapshot::isUsed)
				.toList();

		return new PackingStatistics(
				usedVehicles.stream().mapToInt(v -> v.placedPackages().size()).sum(),
				usedVehicles.size(),
				unpackedPackages.isEmpty(),
				usedVehicles.stream().mapToDouble(PackedVehicleSnapshot::volumeUtilization).average().orElse(0.0),
				usedVehicles.stream().mapToDouble(PackedVehicleSnapshot::weightUtilization).average().orElse(0.0)
		);
	}
}
