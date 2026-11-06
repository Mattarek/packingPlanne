package org.example.packing.domain.exception;

public final class PackageNotAcceptedException extends BusinessRuleViolationException {

	public PackageNotAcceptedException(final String reason) {
		super("Package not accepted: " + reason);
	}
}
