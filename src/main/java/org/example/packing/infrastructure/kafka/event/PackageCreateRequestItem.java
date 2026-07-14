package org.example.packing.infrastructure.kafka.event;

import jakarta.validation.constraints.Positive;

public record PackageCreateRequestItem(
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
