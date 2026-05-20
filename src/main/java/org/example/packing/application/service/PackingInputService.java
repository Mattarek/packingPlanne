package org.example.packing.application.service;

import org.example.packing.application.dto.PackingInputRequest;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.example.packing.infrastructure.persistence.entity.VehicleEntity;
import org.example.packing.infrastructure.persistence.mapper.PackagePersistenceMapper;
import org.example.packing.infrastructure.persistence.mapper.VehiclePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.example.packing.infrastructure.persistence.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class PackingInputService {

	private final VehicleRepository vehicleRepository;
	private final PackageRepository packageRepository;
	private final VehiclePersistenceMapper vehicleMapper;
	private final PackagePersistenceMapper packageMapper;

	public PackingInputService(
			final VehicleRepository vehicleRepository,
			final PackageRepository packageRepository,
			final VehiclePersistenceMapper vehicleMapper,
			final PackagePersistenceMapper packageMapper
	) {
		this.vehicleRepository = vehicleRepository;
		this.packageRepository = packageRepository;
		this.vehicleMapper = vehicleMapper;
		this.packageMapper = packageMapper;
	}

	@Transactional
	public void saveInput(final PackingInputRequest request) {
		Objects.requireNonNull(request, "request must not be null");

		final List<VehicleEntity> vehicles = vehicleMapper.toEntityList(request.vehicles());
		final List<PackageEntity> packages = packageMapper.toEntityList(request.packages());

		vehicleRepository.saveAll(vehicles);
		packageRepository.saveAll(packages);
	}
}