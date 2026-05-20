package org.example.packing.application.dto;

public record PackageRequest(
		String id,
		double lengthCm,
		double widthCm,
		double heightCm,
		double weightKg
) {
}