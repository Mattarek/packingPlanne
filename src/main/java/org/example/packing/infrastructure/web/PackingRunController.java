package org.example.packing.infrastructure.web;

import org.example.packing.application.port.in.RunPackingUseCase;
import org.example.packing.infrastructure.web.dto.ApiResponse;
import org.example.packing.infrastructure.web.dto.PackingRunResponse;
import org.example.packing.infrastructure.web.mapper.RestResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packing-runs")
public class PackingRunController {

	private final RunPackingUseCase runPackingUseCase;
	private final RestResponseMapper responseMapper;

	public PackingRunController(
			final RunPackingUseCase runPackingUseCase,
			final RestResponseMapper responseMapper
	) {
		this.runPackingUseCase = runPackingUseCase;
		this.responseMapper = responseMapper;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PackingRunResponse>> runPacking() {
		final PackingRunResponse response = responseMapper.toResponse(runPackingUseCase.runPacking());

		return ResponseEntity.ok(
				ApiResponse.success("Packing run completed successfully.", response)
		);
	}
}
