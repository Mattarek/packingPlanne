package org.example.packing.domain.model;

/**
 * How fragile a package's contents are, from most to least robust.
 * <p>
 * {@code ULTRA_FRAGILE} items are rejected by company policy — see
 * {@link org.example.packing.domain.policy.PackageAcceptancePolicy}.
 */
public enum FragilityLevel {
	STANDARD,
	FRAGILE,
	ULTRA_FRAGILE
}
