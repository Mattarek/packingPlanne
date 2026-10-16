package org.example.packing.application.service;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.port.PackingInputLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PackingRunnerService {

	private final PackingInputLoader packingInputLoader;
	private final PackingService packingService;

	public PackingRunnerService(
			final PackingInputLoader packingInputLoader,
			final PackingService packingService
	) {
		this.packingInputLoader = packingInputLoader;
		this.packingService = packingService;
	}

	@Transactional(readOnly = true)
	public PackingReport runPacking() {
		return packingService.pack(
				packingInputLoader.loadPackages(),
				packingInputLoader.loadVehicles()
		);
	}
}
