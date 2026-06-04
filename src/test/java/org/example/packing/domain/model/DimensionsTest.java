package org.example.packing.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DimensionsTest {

	@Test
	void shouldCreateDimensions() {
		final Dimensions dimensions = new Dimensions(100.0, 50.0, 30.0);

		assertEquals(100.0, dimensions.length());
		assertEquals(50.0, dimensions.width());
		assertEquals(30.0, dimensions.height());
	}

	@Test
	void shouldCalculateVolume() {
		final Dimensions dimensions = new Dimensions(10.0, 20.0, 30.0);

		assertEquals(6000.0, dimensions.volume());
	}

	@Test
	void shouldRejectNegativeLength() {
		assertThrows(IllegalArgumentException.class, () -> new Dimensions(-1.0, 20.0, 30.0));
	}

	@Test
	void shouldRejectNegativeWidth() {
		assertThrows(IllegalArgumentException.class, () -> new Dimensions(10.0, -1.0, 30.0));
	}

	@Test
	void shouldRejectNegativeHeight() {
		assertThrows(IllegalArgumentException.class, () -> new Dimensions(10.0, 20.0, -1.0));
	}

	@Test
	void shouldRejectZeroLength() {
		assertThrows(IllegalArgumentException.class, () -> new Dimensions(0.0, 20.0, 30.0));
	}

	@Test
	void shouldRejectNaN() {
		assertThrows(IllegalArgumentException.class, () -> new Dimensions(Double.NaN, 20.0, 30.0));
	}

	@Test
	void shouldRejectInfinity() {
		assertThrows(IllegalArgumentException.class, () -> new Dimensions(Double.POSITIVE_INFINITY, 20.0, 30.0));
	}
}
