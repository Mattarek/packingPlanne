package org.example.packing.application.command;

public record CreateVehicleCommand(
		String name,
		double length,
		double width,
		double height,
		double maxPayload
) {
}
