package org.example.packing.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WeightTest {
	@Test
	void shouldCreateWeight() {
		final Weight weight = new Weight(10.5);

		assertEquals(10.5, weight.kilograms());
	}

	@Test
	void shouldCreateZeroWeight() {
		final Weight weight = Weight.ZERO;

		assertEquals(0.0, weight.kilograms());
	}

	@Test
	void shouldRejectNegativeWeight() {
		assertThrows(IllegalArgumentException.class, () -> new Weight(-1.0));
	}

	@Test
	void shouldRejectNaN() {
		assertThrows(IllegalArgumentException.class, () -> new Weight(Double.NaN));
	}

	@Test
	void shouldRejectInfinity() {
		assertThrows(IllegalArgumentException.class, () -> new Weight(Double.POSITIVE_INFINITY));
	}

	@Test
	void shouldAddWeights() {
		final Weight first = new Weight(10.0);
		final Weight second = new Weight(15.0);

		final Weight result = first.add(second);

		assertEquals(new Weight(25.0), result);
	}

	@Test
	void shouldCheckLessThanOrEqualTo() {
		final Weight lighter = new Weight(10.0);
		final Weight heavier = new Weight(20.0);

		assertTrue(lighter.isLessThanOrEqualTo(heavier));
		assertTrue(lighter.isLessThanOrEqualTo(new Weight(10.0)));
		assertFalse(heavier.isLessThanOrEqualTo(lighter));
	}

	@Test
	void shouldCompareWeights() {
		final Weight lighter = new Weight(10.0);
		final Weight heavier = new Weight(20.0);

		assertTrue(lighter.compareTo(heavier) < 0);
		assertTrue(heavier.compareTo(lighter) > 0);
		assertEquals(0, lighter.compareTo(new Weight(10.0)));
	}
}
