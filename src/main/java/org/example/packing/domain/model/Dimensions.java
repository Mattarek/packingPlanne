package org.example.packing.domain.model;

import java.util.Objects;

/**
 * Immutable value object representing 3D dimensions in centimeters.
 * <p>
 * Invariants:
 * <ul>
 *   <li>All dimensions must be positive</li>
 *   <li>Object is immutable — safe for concurrent use</li>
 * </ul>
 */

public record Dimensions(double length, double width, double height) {

	public Dimensions {
		if (!isPositiveFinite(length) || !isPositiveFinite(width) || !isPositiveFinite(height)) {
			throw new IllegalArgumentException(
					"All dimensions must be positive finite numbers. Got: l=%.2f w=%.2f h=%.2f"
							.formatted(length, width, height));
		}
	}

	private static boolean isPositiveFinite(final double value) {
		return value > 0
				&& !Double.isNaN(value)
				&& !Double.isInfinite(value);
	}

	public double volume() {
		return length * width * height;
	}

	/**
	 * Checks whether a box of these dimensions fits inside the given outer dimensions
	 * without rotation.
	 */
	public boolean fitsInside(final Dimensions outer) {
		Objects.requireNonNull(outer, "outer dimensions must not be null");
		return length <= outer.length
				&& width <= outer.width
				&& height <= outer.height;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Dimensions(final double length1, final double width1, final double height1))) {
			return false;
		}
		return Double.compare(length, length1) == 0
				&& Double.compare(width, width1) == 0
				&& Double.compare(height, height1) == 0;
	}

	@Override
	public String toString() {
		return "%.1f×%.1f×%.1f cm".formatted(length, width, height);
	}
}