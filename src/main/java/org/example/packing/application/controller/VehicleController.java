package org.example.packing.application.controller;

import jakarta.validation.Valid;
import org.example.packing.application.dto.ApiResponse;
import org.example.packing.application.dto.DeleteResponse;
import org.example.packing.application.dto.PageResponse;
import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.application.service.VehicleService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

	private static final String DEFAULT_PAGE = "0";
	private static final String DEFAULT_SIZE = "20";

	private final VehicleService vehicleService;

	public VehicleController(final VehicleService vehicleService) {
		this.vehicleService = vehicleService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<List<VehicleResponse>>> createVehicles(
			@Valid @RequestBody final List<VehicleRequest> requests
	) {
		final List<VehicleResponse> response = vehicleService.createVehicles(requests);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(
						ApiResponse.success(
								"Vehicles created successfully.",
								response
						)
				);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getVehicles(
			@RequestParam(defaultValue = DEFAULT_PAGE) final int page,
			@RequestParam(defaultValue = DEFAULT_SIZE) final int size
	) {
		final Page<VehicleResponse> response = vehicleService.getVehicles(page, size);

		return ResponseEntity.ok(
				ApiResponse.success(
						"Vehicles fetched successfully.",
						PageResponse.from(response)
				)
		);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(
			@PathVariable final UUID id
	) {
		final VehicleResponse response = vehicleService.getVehicle(id);

		return ResponseEntity.ok(
				ApiResponse.success(
						"Vehicle fetched successfully.",
						response
				)
		);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<DeleteResponse>> deleteVehicle(
			@PathVariable final UUID id
	) {
		vehicleService.deleteVehicle(id);

		return ResponseEntity.ok(
				ApiResponse.success(
						"Vehicle deleted successfully.",
						new DeleteResponse(id)
				)
		);
	}
}