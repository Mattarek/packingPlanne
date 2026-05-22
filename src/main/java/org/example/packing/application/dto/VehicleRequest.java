package org.example.packing.application.dto;

public record VehicleRequest(
		String id,
		String name,
		double length,
		double width,
		double height,
		double maxPayload
) {
}