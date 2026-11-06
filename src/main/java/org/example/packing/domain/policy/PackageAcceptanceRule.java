package org.example.packing.domain.policy;

import org.example.packing.domain.model.Package;

import java.util.Optional;

@FunctionalInterface
public interface PackageAcceptanceRule {
	Optional<String> findViolation(Package pkg);
}
