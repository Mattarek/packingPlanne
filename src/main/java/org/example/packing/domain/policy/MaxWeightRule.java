package org.example.packing.domain.policy;

import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Weight;

import java.util.Objects;
import java.util.Optional;

public final class MaxWeightRule implements PackageAcceptanceRule {

	private final Weight max;

	public MaxWeightRule(final Weight max) {
		this.max = Objects.requireNonNull(max, "max must not be null");
	}

	@Override
	public Optional<String> findViolation(final Package pkg) {
		if (pkg.weight().isLessThanOrEqualTo(max)) {
			return Optional.empty();
		}
		return Optional.of("weight %s exceeds the maximum accepted weight %s"
				.formatted(pkg.weight(), max));
	}
}
