package org.example.packing.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a package to be loaded.
 * <p>
 * Identity is established by {@code id}. Two packages with the same dimensions
 * and weight but different ids are different packages.
 * <p>
 * {@code category} and {@code fragility} default to {@code STANDARD} where not
 * given explicitly — see the 3-arg constructor/factory below, kept for callers
 * (mostly the packing algorithm) that only care about geometry and weight.
 */
public record Package(UUID id, Dimensions dimensions, Weight weight, ProductCategory category, FragilityLevel fragility) {

	public Package(
			final UUID id,
			final Dimensions dimensions,
			final Weight weight,
			final ProductCategory category,
			final FragilityLevel fragility
	) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.dimensions = Objects.requireNonNull(dimensions, "dimensions must not be null");
		this.weight = Objects.requireNonNull(weight, "weight must not be null");
		this.category = Objects.requireNonNull(category, "category must not be null");
		this.fragility = Objects.requireNonNull(fragility, "fragility must not be null");
	}

	/**
	 * Convenience constructor for callers that don't care about product category
	 * or fragility, defaulting both to {@code STANDARD}.
	 */
	public Package(final UUID id, final Dimensions dimensions, final Weight weight) {
		this(id, dimensions, weight, ProductCategory.STANDARD, FragilityLevel.STANDARD);
	}

	/**
	 * Convenience factory generating a random UUID.
	 */
	public static Package of(final Dimensions dimensions, final Weight weight) {
		return new Package(UUID.randomUUID(), dimensions, weight);
	}

	/**
	 * Convenience factory generating a random UUID.
	 */
	public static Package of(
			final Dimensions dimensions,
			final Weight weight,
			final ProductCategory category,
			final FragilityLevel fragility
	) {
		return new Package(UUID.randomUUID(), dimensions, weight, category, fragility);
	}

	public double volume() {
		return dimensions.volume();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final Package other)) {
			return false;
		}
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "Package[id=%s, %s, %s]".formatted(id, dimensions, weight);
	}
}
