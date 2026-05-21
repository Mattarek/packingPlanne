package org.example.packing.application.dto;

public record PackageResponse(
		String id,
		double lengthCm,
		double widthCm,
		double heightCm,
		double weightKg
) {
}