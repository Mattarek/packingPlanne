package org.example.packing.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.packing.application.port.PageResult;
import org.example.packing.application.port.in.VehicleUseCase;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.infrastructure.web.dto.ApiResponse;
import org.example.packing.infrastructure.web.dto.DeleteResponse;
import org.example.packing.infrastructure.web.dto.PageResponse;
import org.example.packing.infrastructure.web.dto.VehicleRequest;
import org.example.packing.infrastructure.web.dto.VehicleResponse;
import org.example.packing.infrastructure.web.mapper.RestRequestMapper;
import org.example.packing.infrastructure.web.mapper.RestResponseMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
@RequestMapping("/api/vehicles")
public class VehicleController {

	private static final String DEFAULT_PAGE = "0";
	private static final String DEFAULT_SIZE = "20";

	private final VehicleUseCase vehicleUseCase;
	private final RestRequestMapper requestMapper;
	private final RestResponseMapper responseMapper;

	public VehicleController(
			final VehicleUseCase vehicleUseCase,
			final RestRequestMapper requestMapper,
			final RestResponseMapper responseMapper
	) {
		this.vehicleUseCase = vehicleUseCase;
		this.requestMapper = requestMapper;
		this.responseMapper = responseMapper;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<List<VehicleResponse>>> createVehicles(
			@Valid @RequestBody final List<VehicleRequest> requests
	) {
		final List<Vehicle> created = vehicleUseCase.createVehicles(
				requestMapper.toVehicleCommands(requests)
		);
		final List<VehicleResponse> response = responseMapper.toVehicleResponses(created);

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
			@Min(0) @RequestParam(defaultValue = DEFAULT_PAGE) final int page,
			@Min(1) @Max(100) @RequestParam(defaultValue = DEFAULT_SIZE) final int size
	) {
		final PageResult<VehicleResponse> response = vehicleUseCase.getVehicles(page, size)
				.map(responseMapper::toResponse);

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
		final VehicleResponse response = responseMapper.toResponse(vehicleUseCase.getVehicle(id));

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
		vehicleUseCase.deleteVehicle(id);

		return ResponseEntity.ok(
				ApiResponse.success(
						"Vehicle deleted successfully.",
						new DeleteResponse(id)
				)
		);
	}
}