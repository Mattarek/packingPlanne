package org.example.packing.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PackageTest {

	private UUID packageId;

	private Dimensions dimensions;
	private Weight weight;

	private Package pack;

	@BeforeEach
	void setUp() {
		packageId = UUID.randomUUID();

		dimensions = new Dimensions(10.0, 20.0, 30.0);
		weight = mock(Weight.class);

		pack = new Package(packageId, dimensions, weight);
	}

	@Test
	void shouldCreatePackageWhenAllArgumentsAreValid() {
		// when
		final Package result = new Package(packageId, dimensions, weight);

		// then
		assertThat(result.id()).isEqualTo(packageId);
		assertThat(result.dimensions()).isEqualTo(dimensions);
		assertThat(result.weight()).isEqualTo(weight);
	}

	@Test
	void shouldThrowExceptionWhenIdIsNull() {
		// when & then
		assertThatThrownBy(() -> new Package(null, dimensions, weight))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("id must not be null");
	}

	@Test
	void shouldThrowExceptionWhenDimensionsAreNull() {
		// when & then
		assertThatThrownBy(() -> new Package(packageId, null, weight))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("dimensions must not be null");
	}

	@Test
	void shouldThrowExceptionWhenWeightIsNull() {
		// when & then
		assertThatThrownBy(() -> new Package(packageId, dimensions, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("weight must not be null");
	}

	@Test
	void shouldCreatePackageUsingFactoryMethod() {
		// when
		final Package result = Package.of(dimensions, weight);

		// then
		assertThat(result.id()).isNotNull();
		assertThat(result.dimensions()).isEqualTo(dimensions);
		assertThat(result.weight()).isEqualTo(weight);
	}

	@Test
	void shouldCreatePackagesWithDifferentIdsUsingFactoryMethod() {
		// when
		final Package first = Package.of(dimensions, weight);
		final Package second = Package.of(dimensions, weight);

		// then
		assertThat(first.id()).isNotNull();
		assertThat(second.id()).isNotNull();
		assertThat(first.id()).isNotEqualTo(second.id());
	}

	@Test
	void shouldReturnVolumeFromDimensions() {
		// when
		final double result = pack.volume();

		// then
		assertThat(result).isEqualTo(6000.0);
	}

	@Test
	void shouldBeEqualWhenPackagesHaveSameId() {
		// given
		final Dimensions otherDimensions = new Dimensions(1.0, 2.0, 3.0);
		final Weight otherWeight = mock(Weight.class);

		final Package other = new Package(packageId, otherDimensions, otherWeight);

		// when & then
		assertThat(pack).isEqualTo(other);
	}

	@Test
	void shouldNotBeEqualWhenPackagesHaveDifferentIds() {
		// given
		final Package other = new Package(UUID.randomUUID(), dimensions, weight);

		// when & then
		assertThat(pack).isNotEqualTo(other);
	}

	@Test
	void shouldNotBeEqualWhenComparedWithNull() {
		// when & then
		assertThat(pack).isNotEqualTo(null);
	}

	@Test
	void shouldNotBeEqualWhenComparedWithDifferentType() {
		// when & then
		assertThat(pack).isNotEqualTo("package");
	}

	@Test
	void shouldHaveSameHashCodeWhenPackagesHaveSameId() {
		// given
		final Dimensions otherDimensions = new Dimensions(1.0, 2.0, 3.0);
		final Weight otherWeight = mock(Weight.class);

		final Package other = new Package(packageId, otherDimensions, otherWeight);

		// when & then
		assertThat(pack).hasSameHashCodeAs(other);
	}

	@Test
	void shouldHaveDifferentHashCodeWhenPackagesHaveDifferentIds() {
		// given
		final Package other = new Package(UUID.randomUUID(), dimensions, weight);

		// when & then
		assertThat(pack.hashCode()).isNotEqualTo(other.hashCode());
	}

	@Test
	void shouldReturnFormattedString() {
		// given
		when(weight.toString()).thenReturn("5.0 kg");

		// when
		final String result = pack.toString();

		// then
		assertThat(result).isEqualTo(
				"Package[id=%s, %s, %s]".formatted(
						packageId,
						dimensions,
						weight
				)
		);
	}
}