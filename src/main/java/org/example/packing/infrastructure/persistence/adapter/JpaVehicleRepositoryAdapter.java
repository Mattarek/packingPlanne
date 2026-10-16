package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.application.port.VehicleRepository;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.infrastructure.persistence.mapper.VehicleEntityMapper;
import org.example.packing.infrastructure.persistence.repository.VehicleJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements the application's {@link VehicleRepository} port with Spring Data JPA.
 */
@Component
public class JpaVehicleRepositoryAdapter implements VehicleRepository {

	private final VehicleJpaRepository jpaRepository;
	private final VehicleEntityMapper mapper;

	public JpaVehicleRepositoryAdapter(
			final VehicleJpaRepository jpaRepository,
			final VehicleEntityMapper mapper
	) {
		this.jpaRepository = jpaRepository;
		this.mapper = mapper;
	}

	@Override
	public List<Vehicle> saveAll(final List<Vehicle> vehicles) {
		return mapper.toDomainList(jpaRepository.saveAll(mapper.toEntityList(vehicles)));
	}

	@Override
	public Optional<Vehicle> findById(final UUID id) {
		return jpaRepository.findById(id).map(mapper::toDomain);
	}

	@Override
	public Page<Vehicle> findAll(final Pageable pageable) {
		return jpaRepository.findAll(pageable).map(mapper::toDomain);
	}

	@Override
	public boolean existsById(final UUID id) {
		return jpaRepository.existsById(id);
	}

	@Override
	public void deleteById(final UUID id) {
		jpaRepository.deleteById(id);
	}
}
