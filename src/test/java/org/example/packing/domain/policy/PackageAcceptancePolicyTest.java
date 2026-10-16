package org.example.packing.domain.policy;

import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.domain.model.Weight;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackageAcceptancePolicyTest {

	private static final Dimensions MAX_DIMENSIONS = new Dimensions(120.0, 80.0, 80.0);
	private static final Weight MAX_WEIGHT = new Weight(30.0);

	private static final Dimensions ACCEPTED_DIMENSIONS = new Dimensions(50.0, 40.0, 30.0);
	private static final Weight ACCEPTED_WEIGHT = new Weight(10.0);

	private final PackageAcceptancePolicy policy = new PackageAcceptancePolicy(List.of(
			new MaxDimensionsRule(MAX_DIMENSIONS),
			new MaxWeightRule(MAX_WEIGHT),
			new TransportableCategoryRule(),
			new UltraFragileRule()
	));

	@Test
	void shouldAcceptPackageWithinAllLimits() {
		// when & then
		assertThatCode(() -> policy.validate(
				pkg(ACCEPTED_DIMENSIONS, ACCEPTED_WEIGHT, ProductCategory.STANDARD, FragilityLevel.FRAGILE)
		)).doesNotThrowAnyException();
	}

	@Test
	void shouldRejectPackageExceedingMaxDimensions() {
		// given
		final Dimensions tooLarge = new Dimensions(150.0, 40.0, 30.0);

		// when & then
		assertThatThrownBy(() -> policy.validate(
				pkg(tooLarge, ACCEPTED_WEIGHT, ProductCategory.STANDARD, FragilityLevel.STANDARD)
		))
				.isInstanceOf(PackageNotAcceptedException.class)
				.hasMessageContaining("exceed the maximum accepted dimensions");
	}

	@Test
	void shouldAcceptPackageWhoseDimensionsFitTheEnvelopeWhenRotated() {
		// given: same volume/shape as the max envelope, just relabeled axes
		final Dimensions rotated = new Dimensions(80.0, 120.0, 80.0);

		// when & then
		assertThatCode(() -> policy.validate(
				pkg(rotated, ACCEPTED_WEIGHT, ProductCategory.STANDARD, FragilityLevel.STANDARD)
		)).doesNotThrowAnyException();
	}

	@Test
	void shouldRejectPackageExceedingMaxWeight() {
		// given
		final Weight tooHeavy = new Weight(31.0);

		// when & then
		assertThatThrownBy(() -> policy.validate(
				pkg(ACCEPTED_DIMENSIONS, tooHeavy, ProductCategory.STANDARD, FragilityLevel.STANDARD)
		))
				.isInstanceOf(PackageNotAcceptedException.class)
				.hasMessageContaining("exceeds the maximum accepted weight");
	}

	@Test
	void shouldAcceptPackageAtExactlyMaxWeight() {
		// when & then
		assertThatCode(() -> policy.validate(
				pkg(ACCEPTED_DIMENSIONS, MAX_WEIGHT, ProductCategory.STANDARD, FragilityLevel.STANDARD)
		)).doesNotThrowAnyException();
	}

	@Test
	void shouldRejectNonTransportableProductCategory() {
		// when & then
		assertThatThrownBy(() -> policy.validate(
				pkg(ACCEPTED_DIMENSIONS, ACCEPTED_WEIGHT, ProductCategory.HAZARDOUS_MATERIAL, FragilityLevel.STANDARD)
		))
				.isInstanceOf(PackageNotAcceptedException.class)
				.hasMessageContaining("HAZARDOUS_MATERIAL");
	}

	@Test
	void shouldRejectUltraFragilePackage() {
		// when & then
		assertThatThrownBy(() -> policy.validate(
				pkg(ACCEPTED_DIMENSIONS, ACCEPTED_WEIGHT, ProductCategory.STANDARD, FragilityLevel.ULTRA_FRAGILE)
		))
				.isInstanceOf(PackageNotAcceptedException.class)
				.hasMessageContaining("ultra-fragile");
	}

	@Test
	void shouldReportFirstViolatedRuleInConfiguredOrder() {
		// given: violates both the weight and the category rule
		final Package heavyHazardous = pkg(
				ACCEPTED_DIMENSIONS, new Weight(31.0), ProductCategory.HAZARDOUS_MATERIAL, FragilityLevel.STANDARD
		);

		// when & then
		assertThatThrownBy(() -> policy.validate(heavyHazardous))
				.hasMessageContaining("weight");
	}

	@Test
	void shouldApplyLimitsGivenAtConstructionInsteadOfHardCodedOnes() {
		// given: a stricter environment
		final PackageAcceptancePolicy strict = new PackageAcceptancePolicy(List.of(
				new MaxWeightRule(new Weight(5.0))
		));

		// when & then
		assertThatThrownBy(() -> strict.validate(
				pkg(ACCEPTED_DIMENSIONS, ACCEPTED_WEIGHT, ProductCategory.STANDARD, FragilityLevel.STANDARD)
		)).isInstanceOf(PackageNotAcceptedException.class);
	}

	@Test
	void shouldApplyNewRuleWithoutChangingThePolicy() {
		// given: OCP – "FOOD max 20 kg" is just another rule
		final PackageAcceptanceRule foodMax20Kg = pkg -> pkg.category() == ProductCategory.FOOD
				&& !pkg.weight().isLessThanOrEqualTo(new Weight(20.0))
				? Optional.of("food heavier than 20 kg")
				: Optional.empty();
		final PackageAcceptancePolicy withFoodRule = new PackageAcceptancePolicy(List.of(foodMax20Kg));

		// when & then
		assertThatThrownBy(() -> withFoodRule.validate(
				pkg(ACCEPTED_DIMENSIONS, new Weight(25.0), ProductCategory.FOOD, FragilityLevel.STANDARD)
		)).hasMessageContaining("food heavier than 20 kg");
	}

	@Test
	void shouldAcceptEverythingWhenNoRulesAreConfigured() {
		// given
		final PackageAcceptancePolicy noRules = new PackageAcceptancePolicy(List.of());

		// when & then
		assertThatCode(() -> noRules.validate(
				pkg(ACCEPTED_DIMENSIONS, ACCEPTED_WEIGHT, ProductCategory.HAZARDOUS_MATERIAL, FragilityLevel.ULTRA_FRAGILE)
		)).doesNotThrowAnyException();
	}

	@Test
	void shouldRejectNullPackage() {
		// when & then
		assertThatThrownBy(() -> policy.validate(null))
				.isInstanceOf(NullPointerException.class);
	}

	private static Package pkg(
			final Dimensions dimensions,
			final Weight weight,
			final ProductCategory category,
			final FragilityLevel fragility
	) {
		return new Package(UUID.randomUUID(), dimensions, weight, category, fragility);
	}
}
