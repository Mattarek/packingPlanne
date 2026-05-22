package org.example.packing.application.dto;

public record VehicleResponse(
		String id,
		String name,
		double length,
		double width,
		double height,
		double maxPayload
) {
}