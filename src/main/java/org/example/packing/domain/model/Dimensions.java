package org.example.packing.domain.model;

import java.util.Locale;
import java.util.Objects;

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

	public boolean fitsInside(final Dimensions outer) {
		Objects.requireNonNull(outer, "outer dimensions must not be null");
		return length <= outer.length
				&& width <= outer.width
				&& height <= outer.height;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"%.1f×%.1f×%.1f cm",
				length,
				width,
				height
		);
	}
}