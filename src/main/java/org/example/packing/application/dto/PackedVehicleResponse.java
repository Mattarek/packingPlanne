package org.example.packing.application.dto;

import java.util.List;

public record PackedVehicleResponse(
		VehicleResponse vehicle,
		List<PlacedPackageResponse> placedPackages,
		double currentWeightKg,
		double currentVolume,
		double remainingPayloadKg,
		double volumeUtilization,
		double weightUtilization
) {
}