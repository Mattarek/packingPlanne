package org.example.packing.application.controller;

import org.example.packing.application.controller.mapper.RestResponseMapper;
import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.dto.PackingRunResponse;
import org.example.packing.application.service.PackingRunnerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packing-runs")
public class PackingRunController {

	private final PackingRunnerService packingRunnerService;
	private final RestResponseMapper responseMapper;

	public PackingRunController(
			final PackingRunnerService packingRunnerService,
			final RestResponseMapper responseMapper
	) {
		this.packingRunnerService = packingRunnerService;
		this.responseMapper = responseMapper;
	}

	@PostMapping
	public PackingRunResponse runPacking() {
		final PackingReport report = packingRunnerService.runPacking();

		return responseMapper.toResponse(report);
	}
}
