package org.example.packing.application.command;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;

/**
 * Input of the "create package" use case, independent of where the request
 * came from (REST, Kafka, ...).
 */
public record CreatePackageCommand(
		double length,
		double width,
		double height,
		double weight,
		ProductCategory category,
		FragilityLevel fragility
) {
}
