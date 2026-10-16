package org.example.packing.infrastructure.kafka.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Part of the external Kafka contract – category and fragility are plain wire
 * values, translated to domain enums by {@link EventEnumMapper}, so renaming a
 * domain enum constant cannot silently change the message format.
 */
public record PackageCreateRequestItem(
		@Positive(message = "Length must be positive.")
		double length,

		@Positive(message = "Width must be positive.")
		double width,

		@Positive(message = "Height must be positive.")
		double height,

		@Positive(message = "Weight must be positive.")
		double weight,

		@NotBlank(message = "Category must not be blank.")
		String category,

		@NotBlank(message = "Fragility must not be blank.")
		String fragility
) {
}
