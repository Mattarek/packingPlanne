package org.example.packing.application.command;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;

public record CreatePackageCommand(
		double length,
		double width,
		double height,
		double weight,
		ProductCategory category,
		FragilityLevel fragility
) {
}
