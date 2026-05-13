package org.example.packing.infrastructure.demo;

import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.example.packing.infrastructure.persistence.entity.VehicleEntity;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.example.packing.infrastructure.persistence.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(1)
public class DemoDataInitializer implements CommandLineRunner {

	private final VehicleRepository vehicleRepository;
	private final PackageRepository packageRepository;

	public DemoDataInitializer(
			final VehicleRepository vehicleRepository,
			final PackageRepository packageRepository
	) {
		this.vehicleRepository = vehicleRepository;
		this.packageRepository = packageRepository;
	}

	@Override
	public void run(final String... args) {
		if (vehicleRepository.count() == 0) {
			vehicleRepository.saveAll(buildFleet());
		}

		if (packageRepository.count() == 0) {
			packageRepository.saveAll(generatePackages(40, 42L));
		}
	}

	private List<VehicleEntity> buildFleet() {
		final VehicleEntity van = new VehicleEntity(
				"VAN-01",
				"Mercedes Sprinter",
				420,
				180,
				200,
				1000
		);

		final VehicleEntity truck = new VehicleEntity(
				"TRUCK-01",
				"Iveco Daily 7t",
				620,
				220,
				230,
				3500
		);

		final VehicleEntity backupTruck = new VehicleEntity(
				"TRUCK-02",
				"Iveco Daily 7t (backup)",
				620,
				220,
				230,
				3500
		);

		return List.of(van, truck, backupTruck);
	}

	private List<PackageEntity> generatePackages(final int count, final long seed) {
		final Random rng = new Random(seed);
		final List<PackageEntity> packages = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			final double length = 20 + rng.nextDouble() * 100;
			final double width = 20 + rng.nextDouble() * 80;
			final double height = 15 + rng.nextDouble() * 80;
			final double weight = 1 + rng.nextDouble() * 60;

			packages.add(new PackageEntity(
					"PKG-%03d".formatted(i + 1),
					length,
					width,
					height,
					weight
			));
		}

		return packages;
	}
}