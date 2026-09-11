package org.example.packing.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DimensionsTest {

	private Dimensions dimensions;

	@BeforeEach
	void setUp() {
		dimensions = new Dimensions(10.0, 20.0, 30.0);
	}

	@Test
	void shouldCreateDimensionsWhenAllValuesArePositiveAndFinite() {
		// when
		final Dimensions result = new Dimensions(10.0, 20.0, 30.0);

		// then
		assertThat(result.length()).isEqualTo(10.0);
		assertThat(result.width()).isEqualTo(20.0);
		assertThat(result.height()).isEqualTo(30.0);
	}

	@ParameterizedTest
	@ValueSource(doubles = {
			0.0,
			-1.0,
			Double.NaN,
			Double.POSITIVE_INFINITY,
			Double.NEGATIVE_INFINITY
	})
	void shouldThrowExceptionWhenLengthIsNotPositiveFiniteNumber(final double invalidLength) {
		// when & then
		assertThatThrownBy(() -> new Dimensions(invalidLength, 20.0, 30.0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("All dimensions must be positive finite numbers");
	}

	@ParameterizedTest
	@ValueSource(doubles = {
			0.0,
			-1.0,
			Double.NaN,
			Double.POSITIVE_INFINITY,
			Double.NEGATIVE_INFINITY
	})
	void shouldThrowExceptionWhenWidthIsNotPositiveFiniteNumber(final double invalidWidth) {
		// when & then
		assertThatThrownBy(() -> new Dimensions(10.0, invalidWidth, 30.0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("All dimensions must be positive finite numbers");
	}

	@ParameterizedTest
	@ValueSource(doubles = {
			0.0,
			-1.0,
			Double.NaN,
			Double.POSITIVE_INFINITY,
			Double.NEGATIVE_INFINITY
	})
	void shouldThrowExceptionWhenHeightIsNotPositiveFiniteNumber(final double invalidHeight) {
		// when & then
		assertThatThrownBy(() -> new Dimensions(10.0, 20.0, invalidHeight))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("All dimensions must be positive finite numbers");
	}

	@Test
	void shouldCalculateVolume() {
		// when
		final double result = dimensions.volume();

		// then
		assertThat(result).isEqualTo(6000.0);
	}

	@Test
	void shouldFitInsideOuterDimensionsWhenAllDimensionsAreSmaller() {
		// given
		final Dimensions outer = new Dimensions(15.0, 25.0, 35.0);

		// when
		final boolean result = dimensions.fitsInside(outer);

		// then
		assertThat(result).isTrue();
	}

	@Test
	void shouldFitInsideOuterDimensionsWhenAllDimensionsAreEqual() {
		// given
		final Dimensions outer = new Dimensions(10.0, 20.0, 30.0);

		// when
		final boolean result = dimensions.fitsInside(outer);

		// then
		assertThat(result).isTrue();
	}

	@Test
	void shouldNotFitInsideOuterDimensionsWhenLengthIsGreater() {
		// given
		final Dimensions outer = new Dimensions(9.0, 20.0, 30.0);

		// when
		final boolean result = dimensions.fitsInside(outer);

		// then
		assertThat(result).isFalse();
	}

	@Test
	void shouldNotFitInsideOuterDimensionsWhenWidthIsGreater() {
		// given
		final Dimensions outer = new Dimensions(10.0, 19.0, 30.0);

		// when
		final boolean result = dimensions.fitsInside(outer);

		// then
		assertThat(result).isFalse();
	}

	@Test
	void shouldNotFitInsideOuterDimensionsWhenHeightIsGreater() {
		// given
		final Dimensions outer = new Dimensions(10.0, 20.0, 29.0);

		// when
		final boolean result = dimensions.fitsInside(outer);

		// then
		assertThat(result).isFalse();
	}

	@Test
	void shouldThrowExceptionWhenOuterDimensionsAreNull() {
		// when & then
		assertThatThrownBy(() -> dimensions.fitsInside(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("outer dimensions must not be null");
	}

	@Test
	void shouldReturnTrueWhenDimensionsAreEqual() {
		// given
		final Dimensions other = new Dimensions(10.0, 20.0, 30.0);

		// when & then
		assertThat(dimensions).isEqualTo(other);
	}

	@Test
	void shouldReturnFalseWhenDimensionsAreDifferent() {
		// given
		final Dimensions other = new Dimensions(10.0, 20.0, 31.0);

		// when & then
		assertThat(dimensions).isNotEqualTo(other);
	}

	@Test
	void shouldReturnFalseWhenComparedWithNull() {
		// when & then
		assertThat(dimensions).isNotEqualTo(null);
	}

	@Test
	void shouldReturnFalseWhenComparedWithDifferentType() {
		// when & then
		assertThat(dimensions).isNotEqualTo("10.0×20.0×30.0 cm");
	}

	@Test
	void shouldReturnFormattedString() {
		// when
		final String result = dimensions.toString();

		// then
		assertThat(result).isEqualTo("10.0×20.0×30.0 cm");
	}
}