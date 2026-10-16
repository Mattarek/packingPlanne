package org.example.packing.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	void shouldDefaultCategoryAndFragilityToStandardWhenNotGivenExplicitly() {
		// when
		final Package result = new Package(packageId, dimensions, weight);

		// then
		assertThat(result.category()).isEqualTo(ProductCategory.STANDARD);
		assertThat(result.fragility()).isEqualTo(FragilityLevel.STANDARD);
	}

	@Test
	void shouldCreatePackageWithExplicitCategoryAndFragility() {
		// when
		final Package result = new Package(
				packageId,
				dimensions,
				weight,
				ProductCategory.ELECTRONICS,
				FragilityLevel.FRAGILE
		);

		// then
		assertThat(result.id()).isEqualTo(packageId);
		assertThat(result.dimensions()).isEqualTo(dimensions);
		assertThat(result.weight()).isEqualTo(weight);
		assertThat(result.category()).isEqualTo(ProductCategory.ELECTRONICS);
		assertThat(result.fragility()).isEqualTo(FragilityLevel.FRAGILE);
	}

	@Test
	void shouldThrowExceptionWhenCategoryIsNull() {
		// when & then
		assertThatThrownBy(() -> new Package(packageId, dimensions, weight, null, FragilityLevel.STANDARD))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("category must not be null");
	}

	@Test
	void shouldThrowExceptionWhenFragilityIsNull() {
		// when & then
		assertThatThrownBy(() -> new Package(packageId, dimensions, weight, ProductCategory.STANDARD, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("fragility must not be null");
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
	void shouldReturnVolumeFromDimensions() {
		// when
		final double result = pack.volume();

		// then
		assertThat(result).isEqualTo(6000.0);
	}

	// L1: a record promises value semantics – equal components <=> equal objects.

	@Test
	void shouldBeEqualWhenAllComponentsAreEqual() {
		// given
		final Package other = new Package(packageId, dimensions, weight);

		// when & then
		assertThat(pack).isEqualTo(other);
		assertThat(pack).hasSameHashCodeAs(other);
	}

	@Test
	void shouldNotBeEqualWhenSameIdButDifferentDimensions() {
		// given
		final Package resized = new Package(packageId, new Dimensions(1.0, 2.0, 3.0), weight);

		// when & then
		assertThat(pack).isNotEqualTo(resized);
	}

	@Test
	void shouldKeepBothPackagesInHashSetWhenSameIdButDifferentDimensions() {
		// given
		final Package resized = new Package(packageId, new Dimensions(1.0, 2.0, 3.0), weight);

		// when
		final Set<Package> packages = new HashSet<>(List.of(pack, resized));

		// then
		assertThat(packages).hasSize(2);
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
	void shouldIncludeAllComponentsInString() {
		// given
		when(weight.toString()).thenReturn("5.0 kg");

		// when
		final String result = pack.toString();

		// then
		assertThat(result)
				.contains(packageId.toString())
				.contains(dimensions.toString())
				.contains("5.0 kg")
				.contains(ProductCategory.STANDARD.name())
				.contains(FragilityLevel.STANDARD.name());
	}
}
