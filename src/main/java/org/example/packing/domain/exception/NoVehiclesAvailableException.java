package org.example.packing.domain.exception;

public final class NoVehiclesAvailableException extends BusinessRuleViolationException {

	public NoVehiclesAvailableException() {
		super("At least one vehicle must be provided");
	}
}
