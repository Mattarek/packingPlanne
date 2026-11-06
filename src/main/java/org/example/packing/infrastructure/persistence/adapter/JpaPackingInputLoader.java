package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.application.port.out.PackingInputLoader;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.infrastructure.persistence.mapper.PackageEntityMapper;
import org.example.packing.infrastructure.persistence.mapper.VehicleEntityMapper;
import org.example.packing.infrastructure.persistence.repository.PackageJpaRepository;
import org.example.packing.infrastructure.persistence.repository.VehicleJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class JpaPackingInputLoader implements PackingInputLoader {

	private final PackageJpaRepository packageJpaRepository;
	private final VehicleJpaRepository vehicleJpaRepository;
	private final PackageEntityMapper packageMapper;
	private final VehicleEntityMapper vehicleMapper;

	public JpaPackingInputLoader(
			final PackageJpaRepository packageJpaRepository,
			final VehicleJpaRepository vehicleJpaRepository,
			final PackageEntityMapper packageMapper,
			final VehicleEntityMapper vehicleMapper
	) {
		this.packageJpaRepository = packageJpaRepository;
		this.vehicleJpaRepository = vehicleJpaRepository;
		this.packageMapper = packageMapper;
		this.vehicleMapper = vehicleMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Package> loadPackages() {
		return packageMapper.toDomainList(packageJpaRepository.findAll());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Vehicle> loadVehicles() {
		return vehicleMapper.toDomainList(vehicleJpaRepository.findAll());
	}
}
