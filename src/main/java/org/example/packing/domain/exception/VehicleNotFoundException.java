package org.example.packing.domain.exception;

import java.util.UUID;

public class VehicleNotFoundException extends RuntimeException {
	public VehicleNotFoundException(final UUID id) {
		super("Vehicle not found: " + id);
	}
}
