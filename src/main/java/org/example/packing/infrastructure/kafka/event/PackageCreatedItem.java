package org.example.packing.infrastructure.kafka.event;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;

import java.util.UUID;

/**
 * One package inside a {@link PackageCreatedEvent}. Deliberately a separate
 * type from {@code PackageResponse} (the REST DTO) so the published event
 * schema can evolve independently of the web API.
 */
public record PackageCreatedItem(
		UUID id,
		double length,
		double width,
		double height,
		double weight,
		ProductCategory category,
		FragilityLevel fragility
) {
}
