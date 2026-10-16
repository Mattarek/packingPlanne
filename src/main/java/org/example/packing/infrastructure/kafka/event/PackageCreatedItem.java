package org.example.packing.infrastructure.kafka.event;

import java.util.UUID;

/**
 * Part of the external Kafka contract – see {@link PackageCreateRequestItem}.
 */
public record PackageCreatedItem(
		UUID id,
		double length,
		double width,
		double height,
		double weight,
		String category,
		String fragility
) {
}
