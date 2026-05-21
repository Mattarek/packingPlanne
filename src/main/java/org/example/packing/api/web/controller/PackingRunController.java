package org.example.packing.api.web.controller;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.service.PackingRunnerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packing-runs")
public class PackingRunController {

	private final PackingRunnerService packingRunnerService;

	public PackingRunController(final PackingRunnerService packingRunnerService) {
		this.packingRunnerService = packingRunnerService;
	}

	@PostMapping
	public PackingReport runPacking() {
		return packingRunnerService.runPacking();
	}
}