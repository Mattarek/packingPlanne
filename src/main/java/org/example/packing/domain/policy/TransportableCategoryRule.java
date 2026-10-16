package org.example.packing.domain.policy;

import org.example.packing.domain.model.Package;

import java.util.Optional;

public final class TransportableCategoryRule implements PackageAcceptanceRule {

	@Override
	public Optional<String> findViolation(final Package pkg) {
		if (pkg.category().isTransportable()) {
			return Optional.empty();
		}
		return Optional.of("product category %s is not transported".formatted(pkg.category()));
	}
}
