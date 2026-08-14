package org.example.packing.domain.exception;

/**
 * Thrown when a package violates company-wide acceptance policy at intake —
 * e.g. it exceeds the maximum accepted size or weight, or its product
 * category or fragility level is one we do not transport at all.
 * <p>
 * This is distinct from {@link PackageTooLargeException}, which is about a
 * package not fitting a specific vehicle during packing.
 */
public final class PackageNotAcceptedException extends PackingException {

	public PackageNotAcceptedException(final String reason) {
		super("Package not accepted: " + reason);
	}
}
