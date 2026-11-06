package org.example.packing.infrastructure.web.dto;

import java.util.UUID;

public record VehicleResponse(
		UUID id,
		String name,
		double length,
		double width,
		double height,
		double maxPayload
) {
}