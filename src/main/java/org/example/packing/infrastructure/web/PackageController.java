package org.example.packing.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.packing.application.port.PageResult;
import org.example.packing.application.port.in.PackageUseCase;
import org.example.packing.domain.model.Package;
import org.example.packing.infrastructure.web.dto.ApiResponse;
import org.example.packing.infrastructure.web.dto.DeleteResponse;
import org.example.packing.infrastructure.web.dto.PackageRequest;
import org.example.packing.infrastructure.web.dto.PackageResponse;
import org.example.packing.infrastructure.web.dto.PageResponse;
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
@RequestMapping("/api/packages")
public class PackageController {

	private static final String DEFAULT_PAGE = "0";
	private static final String DEFAULT_SIZE = "20";

	private final PackageUseCase packageUseCase;
	private final RestRequestMapper requestMapper;
	private final RestResponseMapper responseMapper;

	public PackageController(
			final PackageUseCase packageUseCase,
			final RestRequestMapper requestMapper,
			final RestResponseMapper responseMapper
	) {
		this.packageUseCase = packageUseCase;
		this.requestMapper = requestMapper;
		this.responseMapper = responseMapper;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<List<PackageResponse>>> createPackages(
			@Valid @RequestBody final List<PackageRequest> requests
	) {
		final List<Package> created = packageUseCase.createPackages(
				requestMapper.toPackageCommands(requests)
		);
		final List<PackageResponse> response = responseMapper.toPackageResponses(created);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Packages created successfully.", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<PackageResponse>>> getPackages(
			@Min(0) @RequestParam(defaultValue = DEFAULT_PAGE) final int page,
			@Min(1) @Max(100) @RequestParam(defaultValue = DEFAULT_SIZE) final int size
	) {
		final PageResult<PackageResponse> response = packageUseCase.getPackages(page, size)
				.map(responseMapper::toResponse);

		return ResponseEntity.ok(
				ApiResponse.success(
						"Packages fetched successfully.",
						PageResponse.from(response)
				)
		);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<PackageResponse>> getPackage(
			@PathVariable final UUID id
	) {
		final PackageResponse response = responseMapper.toResponse(packageUseCase.getPackage(id));

		return ResponseEntity.ok(
				ApiResponse.success("Package fetched successfully.", response)
		);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<DeleteResponse>> deletePackage(
			@PathVariable final UUID id
	) {
		packageUseCase.deletePackage(id);

		return ResponseEntity.ok(
				ApiResponse.success(
						"Package deleted successfully.",
						new DeleteResponse(id)
				)
		);
	}
}