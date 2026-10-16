package org.example.packing.domain.exception;

import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;

public final class PackageTooLargeException extends BusinessRuleViolationException {

	public PackageTooLargeException(final Package pkg, final Vehicle vehicle, final String reason) {
		super("Package %s cannot fit into vehicle %s: %s"
				.formatted(pkg.id(), vehicle.id(), reason));
	}
}
