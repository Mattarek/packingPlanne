package org.example.packing.infrastructure.web.dto;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;

import java.util.UUID;

public record PackageResponse(
		UUID id,
		double length,
		double width,
		double height,
		double weight,
		ProductCategory category,
		FragilityLevel fragility
) {
}
