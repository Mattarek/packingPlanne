package org.example.packing.application.dto;

public record PackageRequest(
		double length,
		double width,
		double height,
		double weight
) {
}