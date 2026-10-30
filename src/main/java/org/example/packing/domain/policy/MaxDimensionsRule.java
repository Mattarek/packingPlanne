package org.example.packing.domain.policy;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;

import java.util.Objects;
import java.util.Optional;

public final class MaxDimensionsRule implements PackageAcceptanceRule {

	private final Dimensions max;

	public MaxDimensionsRule(final Dimensions max) {
		this.max = Objects.requireNonNull(max, "max must not be null");
	}

	@Override
	public Optional<String> findViolation(final Package pkg) {
		if (pkg.dimensions().fitsInside(max)) {
			return Optional.empty();
		}
		return Optional.of("dimensions %s exceed the maximum accepted dimensions %s"
				.formatted(pkg.dimensions(), max));
	}
}
