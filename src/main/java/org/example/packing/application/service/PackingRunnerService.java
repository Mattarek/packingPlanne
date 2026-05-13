package org.example.packing.application.service;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.strategy.ExtremePointPackingStrategy;
import org.example.packing.application.strategy.PackageOrdering;
import org.example.packing.application.strategy.PackingStrategy;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.infrastructure.persistence.mapper.PackagePersistenceMapper;
import org.example.packing.infrastructure.persistence.mapper.VehiclePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.example.packing.infrastructure.persistence.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackingRunnerService {

	private final VehicleRepository vehicleRepository;
	private final PackageRepository packageRepository;
	private final PackingService packingService;

	public PackingRunnerService(
			final VehicleRepository vehicleRepository,
			final PackageRepository packageRepository
	) {
		this.vehicleRepository = vehicleRepository;
		this.packageRepository = packageRepository;

		final PackingStrategy strategy =
				new ExtremePointPackingStrategy(PackageOrdering.VOLUME_DESC);

		packingService = new PackingService(strategy);
	}

	public PackingReport runPacking() {
		final List<Vehicle> fleet = vehicleRepository.findAll()
				.stream()
				.map(VehiclePersistenceMapper::toDomain)
				.toList();

		final List<Package> packages = packageRepository.findAll()
				.stream()
				.map(PackagePersistenceMapper::toDomain)
				.toList();

		return packingService.pack(packages, fleet);
	}
}