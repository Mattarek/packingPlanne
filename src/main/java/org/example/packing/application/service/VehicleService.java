package org.example.packing.application.service;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.domain.exception.VehicleNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.example.packing.infrastructure.persistence.mapper.VehiclePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
	public List<VehicleResponse> createVehicles(final List<VehicleRequest> vehicles) {
		vehicles.forEach(this::validateInvariants);

		final List<VehiclesEntity> entities = vehicleMapper.toEntityList(vehicles);
		final List<VehiclesEntity> savedEntities = vehicleRepository.saveAll(entities);

		return vehicleMapper.toResponseList(savedEntities);
	}

	/**
	 * Builds the domain value objects for a requested vehicle's cargo area and
	 * payload, so their invariants (positive finite dimensions, non-negative
	 * weight) are enforced the same way as for packages — see
	 * {@link PackageService}. Bean Validation on {@link VehicleRequest} already
	 * rejects non-positive values; this additionally catches cases it can't,
	 * such as infinite values.
	 */
	private void validateInvariants(final VehicleRequest request) {
		new Dimensions(request.length(), request.width(), request.height());
		new Weight(request.maxPayload());
	}

	@Transactional(readOnly = true)
	public Page<VehicleResponse> getVehicles(final int page, final int size) {
		final Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.by("id").ascending()
		);

		return vehicleRepository.findAll(pageable)
				.map(vehicleMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public VehicleResponse getVehicle(final UUID id) {
		return vehicleRepository.findById(id)
				.map(vehicleMapper::toResponse)
				.orElseThrow(() -> new VehicleNotFoundException(id));
	}

	@Transactional
	public void deleteVehicle(final UUID id) {
		if (!vehicleRepository.existsById(id)) {
			throw new VehicleNotFoundException(id);
		}

		vehicleRepository.deleteById(id);
	}
}