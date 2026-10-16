package org.example.packing.domain.exception;

import java.util.UUID;

public final class PackageNotFoundException extends NotFoundException {

	public PackageNotFoundException(final UUID id) {
		super("Package not found: " + id);
	}
}
