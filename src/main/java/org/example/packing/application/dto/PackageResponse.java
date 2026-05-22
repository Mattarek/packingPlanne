package org.example.packing.application.dto;

public record PackageResponse(
		String id,
		double length,
		double width,
		double height,
		double weight
) {
}