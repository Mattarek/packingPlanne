package org.example.packing.domain.exception;

import org.example.packing.domain.model.Package;

public final class PackageTooLargeException extends BusinessRuleViolationException {

	public PackageTooLargeException(final Package pkg, final String reason) {
		super("Package %s cannot be packed: %s".formatted(pkg.id(), reason));
	}
}
