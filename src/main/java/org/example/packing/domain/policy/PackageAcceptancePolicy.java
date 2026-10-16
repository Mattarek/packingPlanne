package org.example.packing.domain.policy;

import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.model.Package;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Decides whether the company accepts a package for transport by running it
 * through the configured {@link PackageAcceptanceRule}s, in order. The first
 * violated rule wins.
 */
public final class PackageAcceptancePolicy {

	private final List<PackageAcceptanceRule> rules;

	public PackageAcceptancePolicy(final List<PackageAcceptanceRule> rules) {
		this.rules = List.copyOf(Objects.requireNonNull(rules, "rules must not be null"));
	}

	public void validate(final Package pkg) {
		Objects.requireNonNull(pkg, "pkg must not be null");

		rules.stream()
				.map(rule -> rule.findViolation(pkg))
				.flatMap(Optional::stream)
				.findFirst()
				.ifPresent(reason -> {
					throw new PackageNotAcceptedException(reason);
				});
	}
}
