package org.example.packing.domain.policy;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.Package;

import java.util.Optional;

public final class UltraFragileRule implements PackageAcceptanceRule {

	@Override
	public Optional<String> findViolation(final Package pkg) {
		if (pkg.fragility() != FragilityLevel.ULTRA_FRAGILE) {
			return Optional.empty();
		}
		return Optional.of("ultra-fragile packages are not transported");
	}
}
