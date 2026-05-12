package org.example.packing.infrastructure;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.service.PackingService;
import org.example.packing.application.service.ReportPrinter;
import org.example.packing.application.strategy.ExtremePointPackingStrategy;
import org.example.packing.application.strategy.PackageOrdering;
import org.example.packing.application.strategy.PackingStrategy;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Demo scenario: a small courier company with a mixed fleet
 * (van + small truck) loading a randomized batch of parcels.
 */
public final class Demo {

	private Demo() { /* main entry only */ }

	static void main(final String[] args) {
		final List<Vehicle> fleet = buildFleet();
		final List<Package> packages = generatePackages(40, 42L);

		final PackingStrategy strategy =
				new ExtremePointPackingStrategy(PackageOrdering.VOLUME_DESC);
		final PackingService service = new PackingService(strategy);
		final ReportPrinter printer = new ReportPrinter();

		final PackingReport report = service.pack(packages, fleet);
		System.out.println(printer.print(report));
	}

	private static List<Vehicle> buildFleet() {
		// Real-world dimensions in cm, payload in kg.
		final Vehicle van = new Vehicle(
				"VAN-01",
				"Mercedes Sprinter",
				new Dimensions(420, 180, 200),
				new Weight(1000));

		final Vehicle truck = new Vehicle(
				"TRUCK-01",
				"Iveco Daily 7t",
				new Dimensions(620, 220, 230),
				new Weight(3500));

		final Vehicle backupTruck = new Vehicle(
				"TRUCK-02",
				"Iveco Daily 7t (backup)",
				new Dimensions(620, 220, 230),
				new Weight(3500));

		return List.of(van, truck, backupTruck);
	}

	private static List<Package> generatePackages(final int count, final long seed) {
		final Random rng = new Random(seed);
		final List<Package> packages = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			final double l = 20 + rng.nextDouble() * 100;   // 20–120 cm
			final double w = 20 + rng.nextDouble() * 80;    // 20–100 cm
			final double h = 15 + rng.nextDouble() * 80;    // 15–95 cm
			final double kg = 1 + rng.nextDouble() * 60;    // 1–61 kg
			packages.add(new Package(
					"PKG-%03d".formatted(i + 1),
					new Dimensions(l, w, h),
					new Weight(kg)));
		}
		return packages;
	}
}
