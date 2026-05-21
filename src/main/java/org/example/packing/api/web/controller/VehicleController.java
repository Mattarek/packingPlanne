package org.example.packing.api.web.controller;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.application.service.VehicleService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
	private final VehicleService vehicleService;

	public VehicleController(final VehicleService vehicleService) {
		this.vehicleService = vehicleService;
	}

	@PostMapping
	public void createVehicle(@RequestBody final VehicleRequest request) {
		vehicleService.createVehicle(request);
	}

	@PostMapping("/create")
	public void createVehicles(@RequestBody final List<VehicleRequest> requests) {
		vehicleService.createVehicles(requests);
	}

	@GetMapping
	public List<VehicleResponse> getVehicles() {
		return vehicleService.getVehicles();
	}

	@GetMapping("/{id}")
	public VehicleResponse getVehicle(@PathVariable final String id) {
		return vehicleService.getVehicle(id);
	}

	@DeleteMapping("/{id}")
	public void deleteVehicle(@PathVariable final String id) {
		vehicleService.deleteVehicle(id);
	}

	@DeleteMapping
	public void deleteAllVehicles() {
		vehicleService.deleteAllVehicles();
	}
}
