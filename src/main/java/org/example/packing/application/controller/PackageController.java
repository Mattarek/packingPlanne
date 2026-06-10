package org.example.packing.application.controller;

import jakarta.validation.Valid;
import org.example.packing.application.dto.ApiResponse;
import org.example.packing.application.dto.DeleteResponse;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.dto.PageResponse;
import org.example.packing.application.service.PackageService;
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
@RequestMapping("/api/packages")
public class PackageController {

	private final String defaultPage = "0";
	private final String defaultSize = "20";

	private final PackageService packageService;

	public PackageController(final PackageService packageService) {
		this.packageService = packageService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<List<PackageResponse>>> createPackages(
			@Valid @RequestBody final List<PackageRequest> requests
	) {
		final List<PackageResponse> response = packageService.createPackages(requests);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Packages created successfully.", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<PackageResponse>>> getPackages(
			@RequestParam(defaultValue = defaultPage) final int page,
			@RequestParam(defaultValue = defaultSize) final int size
	) {
		final Page<PackageResponse> response = packageService.getPackages(page, size);

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
		final PackageResponse response = packageService.getPackage(id);

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