package org.example.packing.application.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.packing.application.controller.mapper.RestRequestMapper;
import org.example.packing.application.controller.mapper.RestResponseMapper;
import org.example.packing.application.dto.ApiResponse;
import org.example.packing.application.dto.DeleteResponse;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.dto.PageResponse;
import org.example.packing.application.service.PackageService;
import org.example.packing.domain.model.Package;
import org.springframework.data.domain.Page;
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

	private final PackageService packageService;
	private final RestRequestMapper requestMapper;
	private final RestResponseMapper responseMapper;

	public PackageController(
			final PackageService packageService,
			final RestRequestMapper requestMapper,
			final RestResponseMapper responseMapper
	) {
		this.packageService = packageService;
		this.requestMapper = requestMapper;
		this.responseMapper = responseMapper;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<List<PackageResponse>>> createPackages(
			@Valid @RequestBody final List<PackageRequest> requests
	) {
		final List<Package> created = packageService.createPackages(
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
			@Max(100) @RequestParam(defaultValue = DEFAULT_SIZE) final int size
	) {
		final Page<PackageResponse> response = packageService.getPackages(page, size)
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
		final PackageResponse response = responseMapper.toResponse(packageService.getPackage(id));

		return ResponseEntity.ok(
				ApiResponse.success("Package fetched successfully.", response)
		);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<DeleteResponse>> deletePackage(
			@PathVariable final UUID id
	) {
		packageService.deletePackage(id);

		return ResponseEntity.ok(
				ApiResponse.success(
						"Package deleted successfully.",
						new DeleteResponse(id)
				)
		);
	}
}