package org.example.packing.domain.model;

import java.util.Objects;

/**
 * Immutable value object representing a package placed at a specific position inside a vehicle.
 * <p>
 * The position refers to the bottom-front-left corner of the package's bounding box.
 */
public final class PlacedPackage {

    private final Package pkg;
    private final Position position;

    public PlacedPackage(Package pkg, Position position) {
        this.pkg = Objects.requireNonNull(pkg, "pkg must not be null");
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    public Package pkg() { return pkg; }
    public Position position() { return position; }

    /** X-coordinate of the far end of the package along the length axis. */
    public double endX() { return position.x() + pkg.dimensions().length(); }

    /** Y-coordinate of the far end of the package along the width axis. */
    public double endY() { return position.y() + pkg.dimensions().width(); }

    /** Z-coordinate of the top of the package along the height axis. */
    public double endZ() { return position.z() + pkg.dimensions().height(); }

    /**
     * Tests whether this placed package overlaps with another in 3D space.
     * Two axis-aligned boxes overlap iff they overlap on every axis.
     */
    public boolean overlapsWith(PlacedPackage other) {
        Objects.requireNonNull(other, "other must not be null");
        boolean separateX = this.endX() <= other.position.x() || other.endX() <= this.position.x();
        boolean separateY = this.endY() <= other.position.y() || other.endY() <= this.position.y();
        boolean separateZ = this.endZ() <= other.position.z() || other.endZ() <= this.position.z();
        return !(separateX || separateY || separateZ);
    }

    /**
     * Tests whether this placed package fits entirely inside the given outer dimensions
     * starting from origin (0,0,0).
     */
    public boolean fitsInside(Dimensions cargoArea) {
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
