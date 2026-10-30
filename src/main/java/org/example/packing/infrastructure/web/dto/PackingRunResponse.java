package org.example.packing.infrastructure.web.dto;

import java.util.List;

public record PackingRunResponse(
		String strategyName,
		List<PackedVehicleResponse> packedVehicles,
		List<PackageResponse> unpackedPackages,
		int totalPackagesIn,
		int packedPackagesCount,
		int vehiclesUsed,
		long elapsedMillis,
		boolean fullyPacked,
		double averageVolumeUtilization,
		double averageWeightUtilization
) {
}