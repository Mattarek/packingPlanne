package org.example.packing.domain.policy;

import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.domain.model.Weight;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackageAcceptancePolicyTest {

	private static final Dimensions ACCEPTED_DIMENSIONS = new Dimensions(50.0, 40.0, 30.0);
	private static final Weight ACCEPTED_WEIGHT = new Weight(10.0);

	@Test
	void shouldAcceptPackageWithinAllLimits() {
		// when & then
		assertThatCode(() -> PackageAcceptancePolicy.validate(
				ACCEPTED_DIMENSIONS,
				ACCEPTED_WEIGHT,
				ProductCategory.STANDARD,
				FragilityLevel.FRAGILE
		)).doesNotThrowAnyException();
	}

	@Test
	void shouldRejectPackageExceedingMaxDimensions() {
		// given
		final Dimensions tooLarge = new Dimensions(150.0, 40.0, 30.0);

		// when & then
		assertThatThrownBy(() -> PackageAcceptancePolicy.validate(
				tooLarge,
				ACCEPTED_WEIGHT,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		)).isInstanceOf(PackageNotAcceptedException.class);
	}

	@Test
	void shouldAcceptPackageWhoseDimensionsFitTheEnvelopeWhenRotated() {
		// given: same volume/shape as the max envelope, just relabeled axes
		final Dimensions rotated = new Dimensions(80.0, 120.0, 80.0);

		// when & then
		assertThatCode(() -> PackageAcceptancePolicy.validate(
				rotated,
				ACCEPTED_WEIGHT,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		)).doesNotThrowAnyException();
	}

	@Test
	void shouldRejectPackageExceedingMaxWeight() {
		// given
		final Weight tooHeavy = new Weight(31.0);

		// when & then
		assertThatThrownBy(() -> PackageAcceptancePolicy.validate(
				ACCEPTED_DIMENSIONS,
				tooHeavy,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		)).isInstanceOf(PackageNotAcceptedException.class);
	}

	@Test
	void shouldAcceptPackageAtExactlyMaxWeight() {
		// when & then
		assertThatCode(() -> PackageAcceptancePolicy.validate(
				ACCEPTED_DIMENSIONS,
				PackageAcceptancePolicy.MAX_ACCEPTED_WEIGHT,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		)).doesNotThrowAnyException();
	}

	@Test
	void shouldRejectNonTransportableProductCategory() {
		// when & then
		assertThatThrownBy(() -> PackageAcceptancePolicy.validate(
				ACCEPTED_DIMENSIONS,
				ACCEPTED_WEIGHT,
				ProductCategory.HAZARDOUS_MATERIAL,
				FragilityLevel.STANDARD
		)).isInstanceOf(PackageNotAcceptedException.class);
	}

	@Test
	void shouldRejectUltraFragilePackage() {
		// when & then
		assertThatThrownBy(() -> PackageAcceptancePolicy.validate(
				ACCEPTED_DIMENSIONS,
				ACCEPTED_WEIGHT,
				ProductCategory.STANDARD,
				FragilityLevel.ULTRA_FRAGILE
		)).isInstanceOf(PackageNotAcceptedException.class);
	}

	@Test
	void shouldRejectNullDimensions() {
		// when & then
		assertThatThrownBy(() -> PackageAcceptancePolicy.validate(
				null,
				ACCEPTED_WEIGHT,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void shouldRejectNullWeight() {
		// when & then
		assertThatThrownBy(() -> PackageAcceptancePolicy.validate(
				ACCEPTED_DIMENSIONS,
				null,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void shouldRejectNullCategory() {
		// when & then
		assertThatThrownBy(() -> PackageAcceptancePolicy.validate(
				ACCEPTED_DIMENSIONS,
				ACCEPTED_WEIGHT,
				null,
				FragilityLevel.STANDARD
		)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void shouldRejectNullFragility() {
		// when & then
		assertThatThrownBy(() -> PackageAcceptancePolicy.validate(
				ACCEPTED_DIMENSIONS,
				ACCEPTED_WEIGHT,
				ProductCategory.STANDARD,
				null
		)).isInstanceOf(NullPointerException.class);
	}
}
