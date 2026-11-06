package org.example.packing.domain.model;

import java.util.Objects;

public record PlacedPackage(Package pkg, Position position) {

	public PlacedPackage(final Package pkg, final Position position) {
		this.pkg = Objects.requireNonNull(pkg, "pkg must not be null");
		this.position = Objects.requireNonNull(position, "position must not be null");
	}

	public double endX() {
		return position.x() + pkg.dimensions().length();
	}

	public double endY() {
		return position.y() + pkg.dimensions().width();
	}

	public double endZ() {
		return position.z() + pkg.dimensions().height();
	}

	public boolean overlapsWith(final PlacedPackage other) {
		Objects.requireNonNull(other, "other must not be null");
		final boolean separateX = endX() <= other.position.x() || other.endX() <= position.x();
		final boolean separateY = endY() <= other.position.y() || other.endY() <= position.y();
		final boolean separateZ = endZ() <= other.position.z() || other.endZ() <= position.z();
		return !(separateX || separateY || separateZ);
	}

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
