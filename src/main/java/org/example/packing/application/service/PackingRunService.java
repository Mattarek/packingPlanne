package org.example.packing.application.service;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.port.in.RunPackingUseCase;
import org.example.packing.application.port.out.PackingInputLoader;
import org.springframework.stereotype.Service;

@Service
public class PackingRunService implements RunPackingUseCase {

	private final PackingInputLoader packingInputLoader;
	private final PackingPlanner packingPlanner;

	public PackingRunService(
			final PackingInputLoader packingInputLoader,
			final PackingPlanner packingPlanner
	) {
		this.packingInputLoader = packingInputLoader;
		this.packingPlanner = packingPlanner;
	}

	@Override
	public PackingReport runPacking() {
		return packingPlanner.pack(
				packingInputLoader.loadPackages(),
				packingInputLoader.loadVehicles()
		);
	}
}
