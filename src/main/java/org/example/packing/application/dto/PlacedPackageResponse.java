package org.example.packing.application.dto;

public record PlacedPackageResponse(
		PackageResponse pkg,
		PositionResponse position
) {
}