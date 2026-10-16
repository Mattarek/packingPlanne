package org.example.packing.application.command;

/**
 * Input of the "create vehicle" use case.
 */
public record CreateVehicleCommand(
		String name,
		double length,
		double width,
		double height,
		double maxPayload
) {
}
