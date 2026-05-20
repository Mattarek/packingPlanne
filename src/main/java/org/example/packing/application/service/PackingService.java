package org.example.packing.application.service;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.strategy.PackingStrategy;
import org.example.packing.domain.exception.PackageTooLargeException;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Application service orchestrating the packing operation.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Pre-validate that no single package is impossible to pack</li>
 *   <li>Delegate the actual packing to the configured {@link PackingStrategy}</li>
 *   <li>Measure elapsed time and produce a {@link PackingReport}</li>
 * </ul>
 * <p>
 * The strategy is injected — clients can swap algorithms without changing this class
 * (Open/Closed Principle, Dependency Inversion).
 */
@Service
public final class PackingService {

	private final PackingStrategy strategy;

	public PackingService(final PackingStrategy strategy) {
		this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
	}

	/**
	 * Validates and packs the given packages into the given vehicles.
	 *
	 * @throws PackageTooLargeException if any package is too large/heavy for every vehicle
	 */
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

	/**
	 * Verifies upfront that every package can theoretically fit in at least one vehicle.
	 * This catches client errors early rather than having packages silently end up
	 * in the unpacked list.
	 */
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
