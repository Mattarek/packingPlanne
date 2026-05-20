package org.example.packing.api.web.controller;

import org.example.packing.application.dto.PackingInputRequest;
import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.service.PackingInputService;
import org.example.packing.application.service.PackingRunnerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packing")
public class PackingController {
	private final PackingInputService packingInputService;
	private final PackingRunnerService packingRunnerService;

	public PackingController(final PackingInputService packingInputService, final PackingRunnerService packingRunnerService) {
		this.packingInputService = packingInputService;
		this.packingRunnerService = packingRunnerService;
	}

	@PostMapping("/input")
	public void saveInput(@RequestBody final PackingInputRequest request) {
		packingInputService.saveInput(request);
	}

	@PostMapping("/run")
	public PackingReport runPacing() {
		return packingRunnerService.runPacking();
	}
}
