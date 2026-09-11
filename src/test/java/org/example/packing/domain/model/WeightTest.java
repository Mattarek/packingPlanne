package org.example.packing.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightTest {

	private Weight weight;

	@BeforeEach
	void setUp() {
		weight = new Weight(10.5);
	}

	@Test
	void shouldCreateWeightWhenValueIsPositiveFiniteNumber() {
		// when
		final Weight result = new Weight(10.5);

		// then
		assertThat(result.kilograms()).isEqualTo(10.5);
	}

	@Test
	void shouldCreateZeroWeight() {
		// when
		final Weight result = new Weight(0.0);

		// then
		assertThat(result.kilograms()).isEqualTo(0.0);
	}

	@Test
	void shouldExposeZeroConstant() {
		// when & then
		assertThat(Weight.ZERO).isEqualTo(new Weight(0.0));
		assertThat(Weight.ZERO.kilograms()).isEqualTo(0.0);
	}

	@ParameterizedTest
	@ValueSource(doubles = {
			-1.0,
			Double.NaN,
			Double.POSITIVE_INFINITY,
			Double.NEGATIVE_INFINITY
	})
	void shouldThrowExceptionWhenWeightIsNotNonNegativeFiniteNumber(final double invalidWeight) {
		// when & then
		assertThatThrownBy(() -> new Weight(invalidWeight))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Weight must be a non-negative finite number");
	}

	@Test
	void shouldAddWeights() {
		// given
		final Weight other = new Weight(4.5);

		// when
		final Weight result = weight.add(other);

		// then
		assertThat(result).isEqualTo(new Weight(15.0));
	}

	@Test
	void shouldNotMutateOriginalWeightWhenAdding() {
		// given
		final Weight other = new Weight(4.5);

		// when
		final Weight result = weight.add(other);

		// then
		assertThat(result).isEqualTo(new Weight(15.0));
		assertThat(weight).isEqualTo(new Weight(10.5));
		assertThat(other).isEqualTo(new Weight(4.5));
	}

	@Test
	void shouldThrowExceptionWhenAddingNullWeight() {
		// when & then
		assertThatThrownBy(() -> weight.add(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("other must not be null");
	}

	@Test
	void shouldReturnTrueWhenWeightIsLessThanOtherWeight() {
		// given
		final Weight other = new Weight(20.0);

		// when
		final boolean result = weight.isLessThanOrEqualTo(other);

		// then
		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnTrueWhenWeightIsEqualToOtherWeight() {
		// given
		final Weight other = new Weight(10.5);

		// when
		final boolean result = weight.isLessThanOrEqualTo(other);

		// then
		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenWeightIsGreaterThanOtherWeight() {
		// given
		final Weight other = new Weight(5.0);

		// when
		final boolean result = weight.isLessThanOrEqualTo(other);

		// then
		assertThat(result).isFalse();
	}

	@Test
	void shouldThrowExceptionWhenComparingWithNullUsingIsLessThanOrEqualTo() {
		// when & then
		assertThatThrownBy(() -> weight.isLessThanOrEqualTo(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("other must not be null");
	}

	@Test
	void shouldReturnNegativeNumberWhenComparedWeightIsSmaller() {
		// given
		final Weight other = new Weight(20.0);

		// when
		final int result = weight.compareTo(other);

		// then
		assertThat(result).isNegative();
	}

	@Test
	void shouldReturnZeroWhenComparedWeightIsEqual() {
		// given
		final Weight other = new Weight(10.5);

		// when
		final int result = weight.compareTo(other);

		// then
		assertThat(result).isZero();
	}

	@Test
	void shouldReturnPositiveNumberWhenComparedWeightIsGreater() {
		// given
		final Weight other = new Weight(5.0);

		// when
		final int result = weight.compareTo(other);

		// then
		assertThat(result).isPositive();
	}

	@Test
	void shouldReturnTrueWhenWeightsAreEqual() {
		// given
		final Weight other = new Weight(10.5);

		// when & then
		assertThat(weight).isEqualTo(other);
	}

	@Test
	void shouldReturnFalseWhenWeightsAreDifferent() {
		// given
		final Weight other = new Weight(11.0);

		// when & then
		assertThat(weight).isNotEqualTo(other);
	}

	@Test
	void shouldReturnFalseWhenComparedWithNull() {
		// when & then
		assertThat(weight).isNotEqualTo(null);
	}

	@Test
	void shouldReturnFalseWhenComparedWithDifferentType() {
		// when & then
		assertThat(weight).isNotEqualTo("10.50 kg");
	}

	@Test
	void shouldReturnSameHashCodeForEqualWeights() {
		// given
		final Weight other = new Weight(10.5);

		// when & then
		assertThat(weight).hasSameHashCodeAs(other);
	}

	@Test
	void shouldReturnFormattedString() {
		// when
		final String result = weight.toString();

		// then
		assertThat(result).isEqualTo("10,50 kg");
	}
}