package org.example.packing.domain.model;

import java.util.Locale;

public record Position(double x, double y, double z) {

	public static final Position ORIGIN = new Position(0, 0, 0);

	public Position {
		if (x < 0 || y < 0 || z < 0) {
			throw new IllegalArgumentException(
					"Coordinates must be non-negative. Got: x=%.2f y=%.2f z=%.2f"
							.formatted(x, y, z));
		}
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "(%.1f, %.1f, %.1f)", x, y, z);
	}
}
