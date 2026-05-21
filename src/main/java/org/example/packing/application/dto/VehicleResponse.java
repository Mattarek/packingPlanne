package org.example.packing.application.dto;

public record VehicleResponse(
		String id,
		String name,
		double lengthCm,
		double widthCm,
		double heightCm,
		double maxPayloadKg
) {
}