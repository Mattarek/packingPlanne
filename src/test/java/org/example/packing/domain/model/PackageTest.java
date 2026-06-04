package org.example.packing.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PackageTest {

	@Test
	void shouldCreatePackage() {
		final Dimensions dimensions = new Dimensions(90.0, 60.0, 50.0);
		final Weight weight = new Weight(200.0);

		final Package pkg = new Package("PKG-001", dimensions, weight);

		assertEquals("PKG-001", pkg.id());
		assertEquals(dimensions, pkg.dimensions());
		assertEquals(weight, pkg.weight());
	}

	@Test
	void shouldCalculateVolume() {
		final Package pkg = new Package(
				"PKG-001",
				new Dimensions(90.0, 60.0, 50.0),
				new Weight(200.0)
		);

		assertEquals(270000.0, pkg.volume());
	}

	@Test
	void shouldRejectNullId() {
		assertThrows(NullPointerException.class, () ->
				new Package(null, new Dimensions(90.0, 60.0, 50.0), new Weight(200.0))
		);
	}

	@Test
	void shouldRejectBlankId() {
		assertThrows(IllegalArgumentException.class, () ->
				new Package("   ", new Dimensions(90.0, 60.0, 50.0), new Weight(200.0))
		);
	}

	@Test
	void shouldRejectNullDimensions() {
		assertThrows(NullPointerException.class, () ->
				new Package("PKG-001", null, new Weight(200.0))
		);
	}

	@Test
	void shouldRejectNullWeight() {
		assertThrows(NullPointerException.class, () ->
				new Package("PKG-001", new Dimensions(90.0, 60.0, 50.0), null)
		);
	}

	@Test
	void packagesWithSameIdShouldBeEqual() {
		final Package first = new Package(
				"PKG-001",
				new Dimensions(90.0, 60.0, 50.0),
				new Weight(200.0)
		);

		final Package second = new Package(
				"PKG-001",
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(5.0)
		);

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
	}

	@Test
	void packagesWithDifferentIdsShouldNotBeEqual() {
		final Package first = new Package(
				"PKG-001",
				new Dimensions(90.0, 60.0, 50.0),
				new Weight(200.0)
		);

		final Package second = new Package(
				"PKG-002",
				new Dimensions(90.0, 60.0, 50.0),
				new Weight(200.0)
		);

		assertNotEquals(first, second);
	}
}