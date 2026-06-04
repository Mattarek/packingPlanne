package org.example.packing.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackedVehicleTest {

	@Test
	void shouldCreatePackedVehicleWithEmptyPackagesAndZeroWeight() {
		final Vehicle vehicle = createVan();

		final PackedVehicle packedVehicle = new PackedVehicle(vehicle);

		assertEquals(vehicle, packedVehicle.vehicle());
		assertTrue(packedVehicle.placedPackages().isEmpty());
		assertEquals(Weight.ZERO, packedVehicle.currentWeight());
	}

	@Test
	void shouldPlacePackageWhenItFits() {
		final PackedVehicle packedVehicle = new PackedVehicle(createVan());

		final Package pkg = new Package(
				"PKG-001",
				new Dimensions(90.0, 60.0, 50.0),
				new Weight(200.0)
		);

		final Position position = new Position(0.0, 0.0, 0.0);

		assertTrue(packedVehicle.canPlace(pkg, position));

		packedVehicle.place(pkg, position);

		assertEquals(1, packedVehicle.placedPackages().size());
		assertEquals(new Weight(200.0), packedVehicle.currentWeight());
	}

	@Test
	void shouldNotPlacePackageWhenPayloadWouldBeExceeded() {
		final Vehicle vehicle = new Vehicle(
				"SMALL-VAN",
				"Small Van",
				new Dimensions(420.0, 180.0, 200.0),
				new Weight(100.0)
		);

		final PackedVehicle packedVehicle = new PackedVehicle(vehicle);

		final Package pkg = new Package(
				"PKG-001",
				new Dimensions(90.0, 60.0, 50.0),
				new Weight(200.0)
		);

		assertFalse(packedVehicle.canPlace(pkg, new Position(0.0, 0.0, 0.0)));
	}

	@Test
	void shouldNotPlacePackageWhenItDoesNotFitInsideCargoArea() {
		final PackedVehicle packedVehicle = new PackedVehicle(createVan());

		final Package tooLongPackage = new Package(
				"PKG-TOO-LONG",
				new Dimensions(600.0, 60.0, 50.0),
				new Weight(200.0)
		);

		assertFalse(packedVehicle.canPlace(tooLongPackage, new Position(0.0, 0.0, 0.0)));
	}

	@Test
	void shouldNotPlaceOverlappingPackages() {
		final PackedVehicle packedVehicle = new PackedVehicle(createVan());

		final Package first = new Package(
				"PKG-001",
				new Dimensions(100.0, 100.0, 100.0),
				new Weight(100.0)
		);

		final Package second = new Package(
				"PKG-002",
				new Dimensions(100.0, 100.0, 100.0),
				new Weight(100.0)
		);

		packedVehicle.place(first, new Position(0.0, 0.0, 0.0));

		assertFalse(packedVehicle.canPlace(second, new Position(50.0, 50.0, 50.0)));
	}

	@Test
	void shouldPlacePackagesNextToEachOther() {
		final PackedVehicle packedVehicle = new PackedVehicle(createVan());

		final Package first = new Package(
				"PKG-001",
				new Dimensions(100.0, 100.0, 100.0),
				new Weight(100.0)
		);

		final Package second = new Package(
				"PKG-002",
				new Dimensions(100.0, 100.0, 100.0),
				new Weight(100.0)
		);

		packedVehicle.place(first, new Position(0.0, 0.0, 0.0));

		assertTrue(packedVehicle.canPlace(second, new Position(100.0, 0.0, 0.0)));
	}

	@Test
	void shouldCalculateRemainingPayload() {
		final PackedVehicle packedVehicle = new PackedVehicle(createVan());

		final Package pkg = new Package(
				"PKG-001",
				new Dimensions(90.0, 60.0, 50.0),
				new Weight(200.0)
		);

		packedVehicle.place(pkg, new Position(0.0, 0.0, 0.0));

		assertEquals(new Weight(800.0), packedVehicle.remainingPayload());
	}

	@Test
	void shouldCalculateCurrentVolume() {
		final PackedVehicle packedVehicle = new PackedVehicle(createVan());

		final Package pkg = new Package(
				"PKG-001",
				new Dimensions(10.0, 20.0, 30.0),
				new Weight(100.0)
		);

		packedVehicle.place(pkg, new Position(0.0, 0.0, 0.0));

		assertEquals(6000.0, packedVehicle.currentVolume());
	}

	private Vehicle createVan() {
		return new Vehicle(
				"VAN-001",
				"Mercedes Sprinter",
				new Dimensions(420.0, 180.0, 200.0),
				new Weight(1000.0)
		);
	}
}