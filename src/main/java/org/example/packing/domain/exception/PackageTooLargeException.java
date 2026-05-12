package org.example.packing.domain.exception;

import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;

/**
 * Thrown when a single package physically cannot fit into a vehicle —
 * either by dimensions or by exceeding maximum payload alone.
 */
public final class PackageTooLargeException extends PackingException {

	public PackageTooLargeException(final Package pkg, final Vehicle vehicle, final String reason) {
		super("Package %s cannot fit into vehicle %s: %s"
				.formatted(pkg.id(), vehicle.id(), reason));
	}
}
