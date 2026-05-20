package org.example.packing.application.dto;

import java.util.List;

public record PackingInputRequest(
		List<VehicleRequest> vehicles,
		List<PackageRequest> packages
) {
}