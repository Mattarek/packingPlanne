package org.example.packing.domain.model;

import java.util.Objects;

/**
 * Immutable value object representing weight in kilograms.
 */
public record Weight(double kilograms) implements Comparable<Weight> {

	public static final Weight ZERO = new Weight(0.0);

	public Weight {
		if (kilograms < 0 || Double.isNaN(kilograms) || Double.isInfinite(kilograms)) {
			throw new IllegalArgumentException(
					"Weight must be a non-negative finite number. Got: " + kilograms);
		}
	}

	public Weight add(final Weight other) {
		Objects.requireNonNull(other, "other must not be null");
		return new Weight(kilograms + other.kilograms);
	}

	public boolean isLessThanOrEqualTo(final Weight other) {
		Objects.requireNonNull(other, "other must not be null");
		return kilograms <= other.kilograms;
	}

	@Override
	public int compareTo(final Weight other) {
		return Double.compare(kilograms, other.kilograms);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Weight(double kilograms1))) {
			return false;
		}
		return Double.compare(kilograms, kilograms1) == 0;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(kilograms);
	}

	@Override
	public String toString() {
		return "%.2f kg".formatted(kilograms);
	}
}
