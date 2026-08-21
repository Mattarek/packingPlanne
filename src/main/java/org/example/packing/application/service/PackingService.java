package org.example.packing.application.service;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.strategy.PackingStrategy;
import org.example.packing.domain.exception.PackageTooLargeException;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public final class PackingService {

	private final PackingStrategy strategy;

	public PackingService(final PackingStrategy strategy) {
		this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
	}

	public PackingReport pack(final List<Package> packages, final List<Vehicle> vehicles) {
		Objects.requireNonNull(packages, "packages must not be null");
		Objects.requireNonNull(vehicles, "vehicles must not be null");

		if (vehicles.isEmpty()) {
			throw new IllegalArgumentException("At least one vehicle must be provided");
		}

		validateFeasibility(packages, vehicles);

		final long start = System.nanoTime();
		final PackingStrategy.PackingResult result = strategy.pack(packages, vehicles);
		final long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

		return new PackingReport(
				strategy.name(),
				result.packedVehicles(),
				result.unpackedPackages(),
				packages.size(),
				elapsedMillis
		);
	}

	private void validateFeasibility(final List<Package> packages, final List<Vehicle> vehicles) {
		for (final Package pkg : packages) {
			final Vehicle anyFit = vehicles.stream()
					.filter(v -> pkg.dimensions().fitsInside(v.cargoArea()))
					.filter(v -> pkg.weight().isLessThanOrEqualTo(v.maxPayload()))
					.findFirst()
					.orElse(null);

			if (anyFit == null) {
				throw new PackageTooLargeException(
						pkg,
						vehicles.get(0),
						"package does not fit in any provided vehicle"
				);
			}
		}
	}
}
