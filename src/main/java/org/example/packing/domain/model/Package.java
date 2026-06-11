package org.example.packing.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a package to be loaded.
 * <p>
 * Identity is established by {@code id}. Two packages with the same dimensions
 * and weight but different ids are different packages.
 */
public record Package(UUID id, Dimensions dimensions, Weight weight) {

	public Package(final UUID id, final Dimensions dimensions, final Weight weight) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.dimensions = Objects.requireNonNull(dimensions, "dimensions must not be null");
		this.weight = Objects.requireNonNull(weight, "weight must not be null");
	}

	/**
	 * Convenience factory generating a random UUID.
	 */
	public static Package of(final Dimensions dimensions, final Weight weight) {
		return new Package(UUID.randomUUID(), dimensions, weight);
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
