package org.example.packing.application.service;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.example.packing.infrastructure.persistence.mapper.VehiclePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {

	private final VehicleRepository vehicleRepository;
	private final VehiclePersistenceMapper vehicleMapper;

	public VehicleService(
			final VehicleRepository vehicleRepository,
			final VehiclePersistenceMapper vehicleMapper
	) {
		this.vehicleRepository = vehicleRepository;
		this.vehicleMapper = vehicleMapper;
	}

	@Transactional
	public void createVehicle(final VehicleRequest request) {
		final VehiclesEntity entity = vehicleMapper.toEntity(request);
		vehicleRepository.save(entity);
	}

	@Transactional
	public void createVehicles(final List<VehicleRequest> vehicles) {
		final List<VehiclesEntity> entities = vehicleMapper.toEntityList(vehicles);
		vehicleRepository.saveAll(entities);
	}

	@Transactional(readOnly = true)
	public List<VehicleResponse> getVehicles() {
		return vehicleMapper.toResponseList(vehicleRepository.findAll());
	}

	@Transactional(readOnly = true)
	public VehicleResponse getVehicle(final String id) {
		return vehicleRepository.findById(id)
				.map(vehicleMapper::toResponse)
				.orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
	}

	@Transactional
	public void deleteVehicle(final String id) {
		if (!vehicleRepository.existsById(id)) {
			throw new IllegalArgumentException("Vehicle not found: " + id);
		}

		vehicleRepository.deleteById(id);
	}

	@Transactional
	public void deleteAllVehicles() {
		vehicleRepository.deleteAll();
	}
}