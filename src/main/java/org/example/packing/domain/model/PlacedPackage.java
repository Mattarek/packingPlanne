package org.example.packing.domain.model;

import java.util.Objects;

/**
 * Immutable value object representing a package placed at a specific position inside a vehicle.
 * <p>
 * The position refers to the bottom-front-left corner of the package's bounding box.
 */
public record PlacedPackage(Package pkg, Position position) {

	public PlacedPackage(final Package pkg, final Position position) {
		this.pkg = Objects.requireNonNull(pkg, "pkg must not be null");
		this.position = Objects.requireNonNull(position, "position must not be null");
	}

	/**
	 * X-coordinate of the far end of the package along the length axis.
	 */
	public double endX() {
		return position.x() + pkg.dimensions().length();
	}

	/**
	 * Y-coordinate of the far end of the package along the width axis.
	 */
	public double endY() {
		return position.y() + pkg.dimensions().width();
	}

	/**
	 * Z-coordinate of the top of the package along the height axis.
	 */
	public double endZ() {
		return position.z() + pkg.dimensions().height();
	}

	/**
	 * Tests whether this placed package overlaps with another in 3D space.
	 * Two axis-aligned boxes overlap iff they overlap on every axis.
	 */
	public boolean overlapsWith(final PlacedPackage other) {
		Objects.requireNonNull(other, "other must not be null");
		final boolean separateX = endX() <= other.position.x() || other.endX() <= position.x();
		final boolean separateY = endY() <= other.position.y() || other.endY() <= position.y();
		final boolean separateZ = endZ() <= other.position.z() || other.endZ() <= position.z();
		return !(separateX || separateY || separateZ);
	}

	/**
	 * Tests whether this placed package fits entirely inside the given outer dimensions
	 * starting from origin (0,0,0).
	 */
	public boolean fitsInside(final Dimensions cargoArea) {
		Objects.requireNonNull(cargoArea, "cargoArea must not be null");
		return endX() <= cargoArea.length()
				&& endY() <= cargoArea.width()
				&& endZ() <= cargoArea.height();
	}

	@Override
	public String toString() {
		return "PlacedPackage[%s @ %s]".formatted(pkg.id(), position);
	}
}
