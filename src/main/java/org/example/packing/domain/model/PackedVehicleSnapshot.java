package org.example.packing.domain.model;

import java.util.List;
import java.util.Objects;

public record PackedVehicleSnapshot(
		Vehicle vehicle,
		List<PlacedPackage> placedPackages,
		Weight currentWeight,
		double currentVolume,
		Weight remainingPayload,
		double volumeUtilization,
		double weightUtilization
) {

	public PackedVehicleSnapshot {
		Objects.requireNonNull(vehicle, "vehicle must not be null");
		Objects.requireNonNull(currentWeight, "currentWeight must not be null");
		Objects.requireNonNull(remainingPayload, "remainingPayload must not be null");
		placedPackages = List.copyOf(placedPackages);
	}

	public boolean isUsed() {
		return !placedPackages.isEmpty();
	}
}
