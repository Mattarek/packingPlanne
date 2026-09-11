package org.example.packing.application.service;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.infrastructure.persistence.mapper.PackagePersistenceMapper;
import org.example.packing.infrastructure.persistence.mapper.VehiclePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.example.packing.infrastructure.persistence.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PackingRunnerService {

	private final VehicleRepository vehicleRepository;
	private final PackageRepository packageRepository;
	private final VehiclePersistenceMapper vehicleMapper;
	private final PackagePersistenceMapper packageMapper;
	private final PackingService packingService;

	public PackingRunnerService(
			final VehicleRepository vehicleRepository,
			final PackageRepository packageRepository,
			final VehiclePersistenceMapper vehicleMapper,
			final PackagePersistenceMapper packageMapper,
			final PackingService packingService
	) {
		this.vehicleRepository = vehicleRepository;
		this.packageRepository = packageRepository;
		this.vehicleMapper = vehicleMapper;
		this.packageMapper = packageMapper;
		this.packingService = packingService;
	}

	@Transactional(readOnly = true)
	public PackingReport runPacking() {
		final var vehicleEntities = vehicleRepository.findAll();
		final var packageEntities = packageRepository.findAll();
		
		final List<Vehicle> vehicles = vehicleMapper.toDomainList(vehicleEntities);
		final List<Package> packages =
				packageMapper.toDomainList(packageEntities);

		return packingService.pack(packages, vehicles);
	}
}