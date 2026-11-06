package org.example.packing.infrastructure.web.dto;

public record PlacedPackageResponse(
		PackageResponse pkg,
		PositionResponse position
) {
}