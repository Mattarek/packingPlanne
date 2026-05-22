package org.example.packing.api.web.controller;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.dto.PackingRunResponse;
import org.example.packing.application.service.PackingRunnerService;
import org.example.packing.infrastructure.persistence.mapper.PackingReportResponseMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packing-runs")
public class PackingRunController {

	private final PackingRunnerService packingRunnerService;
	private final PackingReportResponseMapper packingReportResponseMapper;

	public PackingRunController(
			final PackingRunnerService packingRunnerService,
			final PackingReportResponseMapper packingReportResponseMapper
	) {
		this.packingRunnerService = packingRunnerService;
		this.packingReportResponseMapper = packingReportResponseMapper;
	}

	@PostMapping
	public PackingRunResponse runPacking() {
		final PackingReport report = packingRunnerService.runPacking();

		return packingReportResponseMapper.toResponse(report);
	}
}