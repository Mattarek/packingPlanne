package org.example.packing.application.dto;

import jakarta.validation.constraints.Positive;

public record PackageRequest(
		@Positive(message = "Length must be positive.")
		double length,

		@Positive(message = "Width must be positive.")
		double width,

		@Positive(message = "Height must be positive.")
		double height,

		@Positive(message = "Weight must be positive.")
		double weight
) {
}