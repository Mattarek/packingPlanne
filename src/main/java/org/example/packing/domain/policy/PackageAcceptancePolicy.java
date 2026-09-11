package org.example.packing.domain.policy;

import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.domain.model.Weight;

import java.util.Arrays;
import java.util.Objects;

public final class PackageAcceptancePolicy {

	public static final Dimensions MAX_ACCEPTED_DIMENSIONS = new Dimensions(120.0, 80.0, 80.0);

	public static final Weight MAX_ACCEPTED_WEIGHT = new Weight(30.0);

	private PackageAcceptancePolicy() {
	}

	public static void validate(
			final Dimensions dimensions,
			final Weight weight,
			final ProductCategory category,
			final FragilityLevel fragility
	) {
		Objects.requireNonNull(dimensions, "dimensions must not be null");
		Objects.requireNonNull(weight, "weight must not be null");
		Objects.requireNonNull(category, "category must not be null");
		Objects.requireNonNull(fragility, "fragility must not be null");

		if (!fitsWithinMaxDimensions(dimensions)) {
			throw new PackageNotAcceptedException(
					"dimensions %s exceed the maximum accepted dimensions %s"
							.formatted(dimensions, MAX_ACCEPTED_DIMENSIONS));
		}

		if (!weight.isLessThanOrEqualTo(MAX_ACCEPTED_WEIGHT)) {
			throw new PackageNotAcceptedException(
					"weight %s exceeds the maximum accepted weight %s"
							.formatted(weight, MAX_ACCEPTED_WEIGHT));
		}

		if (!category.isTransportable()) {
			throw new PackageNotAcceptedException(
					"product category %s is not transported".formatted(category));
		}

		if (fragility == FragilityLevel.ULTRA_FRAGILE) {
			throw new PackageNotAcceptedException("ultra-fragile packages are not transported");
		}
	}

	private static boolean fitsWithinMaxDimensions(final Dimensions dimensions) {
		final double[] actual = sortedAscending(dimensions);
		final double[] max = sortedAscending(MAX_ACCEPTED_DIMENSIONS);

		return actual[0] <= max[0] && actual[1] <= max[1] && actual[2] <= max[2];
	}

	private static double[] sortedAscending(final Dimensions dimensions) {
		final double[] values = {dimensions.length(), dimensions.width(), dimensions.height()};
		Arrays.sort(values);
		return values;
	}
}
