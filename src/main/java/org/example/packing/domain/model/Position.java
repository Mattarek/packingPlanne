package org.example.packing.domain.model;

/**
 * Immutable value object representing a 3D position (origin = bottom-front-left corner of vehicle).
 * Coordinates are in centimeters.
 */
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
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Position(final double x1, final double y1, final double z1))) {
			return false;
		}
		return Double.compare(x, x1) == 0
				&& Double.compare(y, y1) == 0
				&& Double.compare(z, z1) == 0;
	}

	@Override
	public String toString() {
		return "(%.1f, %.1f, %.1f)".formatted(x, y, z);
	}
}
