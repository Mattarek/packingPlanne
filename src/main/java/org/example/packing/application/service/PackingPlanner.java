package org.example.packing.application.service;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.port.out.MonotonicClock;
import org.example.packing.domain.exception.NoVehiclesAvailableException;
import org.example.packing.domain.exception.PackageTooLargeException;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackingStatistics;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.packing.PackingStrategy;

import java.util.List;
import java.util.Objects;

public final class PackingPlanner {

	private static final long NANOS_PER_MILLI = 1_000_000;

	private final PackingStrategy strategy;
	private final MonotonicClock monotonicClock;

	public PackingPlanner(final PackingStrategy strategy, final MonotonicClock monotonicClock) {
		this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
		this.monotonicClock = Objects.requireNonNull(monotonicClock, "monotonicClock must not be null");
	}

	public PackingReport pack(final List<Package> packages, final List<Vehicle> vehicles) {
		Objects.requireNonNull(packages, "packages must not be null");
		Objects.requireNonNull(vehicles, "vehicles must not be null");

		if (vehicles.isEmpty()) {
			throw new NoVehiclesAvailableException();
		}

		validateFeasibility(packages, vehicles);

		final long start = monotonicClock.nanoTime();
		final PackingStrategy.PackingResult result = strategy.pack(packages, vehicles);
		final long elapsedMillis = (monotonicClock.nanoTime() - start) / NANOS_PER_MILLI;

		return new PackingReport(
				strategy.name(),
				result.packedVehicles(),
				result.unpackedPackages(),
				packages.size(),
				elapsedMillis,
				PackingStatistics.of(result.packedVehicles(), result.unpackedPackages())
		);
	}

	private void validateFeasibility(final List<Package> packages, final List<Vehicle> vehicles) {
		for (final Package pkg : packages) {
			if (vehicles.stream().noneMatch(vehicle -> vehicle.canCarry(pkg))) {
				throw new PackageTooLargeException(pkg, "it does not fit in any provided vehicle");
			}
		}
	}
}
