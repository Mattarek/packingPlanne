package org.example.packing.application.controller;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.service.PackageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

	private final PackageService packageService;

	public PackageController(final PackageService packageService) {
		this.packageService = packageService;
	}

	@PostMapping
	public void createPackage(@RequestBody final PackageRequest request) {
		packageService.createPackage(request);
	}

	@PostMapping("/create")
	public void createPackages(@RequestBody final List<PackageRequest> request) {
		packageService.createPackages(request);
	}

	@GetMapping
	public List<PackageResponse> getPackages() {
		return packageService.getPackages();
	}

	@GetMapping("/{id}")
	public PackageResponse getPackage(@PathVariable final String id) {
		return packageService.getPackage(id);
	}

	@DeleteMapping("/{id}")
	public void deletePackage(@PathVariable final String id) {
		packageService.deletePackage(id);
	}

	@DeleteMapping
	public void deleteAllPackages() {
		packageService.deleteAllPackages();
	}
}