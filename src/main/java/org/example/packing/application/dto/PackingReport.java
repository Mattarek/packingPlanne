package org.example.packing.application.dto;

import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicle;

import java.util.List;
import java.util.Objects;

/**
 * Read-only report describing the outcome of a packing operation.
 * Suitable for logging, UI presentation, or API responses.
 */
public record PackingReport(
		String strategyName,
		List<PackedVehicle> packedVehicles,
		List<Package> unpackedPackages,
		int totalPackagesIn,
		long elapsedMillis) {

	public PackingReport {
		Objects.requireNonNull(strategyName, "strategyName must not be null");
		packedVehicles = List.copyOf(packedVehicles);
		unpackedPackages = List.copyOf(unpackedPackages);
	}

	public int packedPackagesCount() {
		return packedVehicles.stream()
				.mapToInt(v -> v.placedPackages().size())
				.sum();
	}

	public int vehiclesUsed() {
		return (int) packedVehicles.stream()
				.filter(v -> !v.placedPackages().isEmpty())
				.count();
	}

	public boolean isFullyPacked() {
		return unpackedPackages.isEmpty();
	}

	public double averageVolumeUtilization() {
		return packedVehicles.stream()
				.filter(v -> !v.placedPackages().isEmpty())
				.mapToDouble(PackedVehicle::volumeUtilization)
				.average()
				.orElse(0.0);
	}

	public double averageWeightUtilization() {
		return packedVehicles.stream()
				.filter(v -> !v.placedPackages().isEmpty())
				.mapToDouble(PackedVehicle::weightUtilization)
				.average()
				.orElse(0.0);
	}
}
