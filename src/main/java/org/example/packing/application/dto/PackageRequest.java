package org.example.packing.application.dto;

public record PackageRequest(
		String id,
		double length,
		double width,
		double height,
		double weight
) {
}