package org.example.packing.application.dto;

import java.util.UUID;

public record VehicleRequest(
		UUID id,
		String name,
		double length,
		double width,
		double height,
		double maxPayload
) {
}