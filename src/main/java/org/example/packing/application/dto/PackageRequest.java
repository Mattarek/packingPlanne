package org.example.packing.application.dto;

import java.util.UUID;

public record PackageRequest(
		UUID id,
		double length,
		double width,
		double height,
		double weight
) {
}