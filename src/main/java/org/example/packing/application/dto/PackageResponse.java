package org.example.packing.application.dto;

import java.util.UUID;

public record PackageResponse(
		UUID id,
		double length,
		double width,
		double height,
		double weight
) {
}