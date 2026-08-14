package org.example.packing.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record VehicleRequest(
		@NotBlank(message = "Name must not be blank.")
		String name,

		@Positive(message = "Length must be positive.")
		double length,

		@Positive(message = "Width must be positive.")
		double width,

		@Positive(message = "Height must be positive.")
		double height,

		@Positive(message = "Max payload must be positive.")
		double maxPayload
) {
}
