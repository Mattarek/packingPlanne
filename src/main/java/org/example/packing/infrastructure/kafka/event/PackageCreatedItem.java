package org.example.packing.infrastructure.kafka.event;

import java.util.UUID;

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
