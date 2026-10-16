package org.example.packing.domain.policy;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class MaxDimensionsRule implements PackageAcceptanceRule {

	private final Dimensions max;

	public MaxDimensionsRule(final Dimensions max) {
		this.max = Objects.requireNonNull(max, "max must not be null");
	}

	private static double[] sortedAscending(final Dimensions dimensions) {
		final double[] values = {dimensions.length(), dimensions.width(), dimensions.height()};
		Arrays.sort(values);
		return values;
	}

	@Override
	public Optional<String> findViolation(final Package pkg) {
		final double[] actual = sortedAscending(pkg.dimensions());
		final double[] limit = sortedAscending(max);

		if (actual[0] <= limit[0] && actual[1] <= limit[1] && actual[2] <= limit[2]) {
			return Optional.empty();
		}
		return Optional.of("dimensions %s exceed the maximum accepted dimensions %s"
				.formatted(pkg.dimensions(), max));
	}
}
