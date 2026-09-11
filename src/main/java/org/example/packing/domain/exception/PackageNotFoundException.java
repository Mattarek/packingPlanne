package org.example.packing.domain.exception;

import java.util.UUID;

public class PackageNotFoundException extends RuntimeException {

	public PackageNotFoundException(final UUID id) {
		super("Package not found: " + id);
	}
}