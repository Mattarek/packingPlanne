package org.example.packing.application.dto;

import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicleSnapshot;
import org.example.packing.domain.model.PackingStatistics;

import java.util.List;
import java.util.Objects;

public record PackingReport(
		String strategyName,
		List<PackedVehicleSnapshot> packedVehicles,
		List<Package> unpackedPackages,
		int totalPackagesIn,
		long elapsedMillis,
		PackingStatistics statistics
) {

	public PackingReport {
		Objects.requireNonNull(strategyName, "strategyName must not be null");
		Objects.requireNonNull(statistics, "statistics must not be null");
		packedVehicles = List.copyOf(packedVehicles);
		unpackedPackages = List.copyOf(unpackedPackages);
	}
}
