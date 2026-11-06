package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.application.port.PageQuery;
import org.example.packing.application.port.PageResult;
import org.example.packing.application.port.out.VehicleRepository;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.infrastructure.persistence.mapper.VehicleEntityMapper;
import org.example.packing.infrastructure.persistence.repository.VehicleJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
	public PageResult<Vehicle> findPage(final PageQuery query) {
		final Page<Vehicle> page = jpaRepository
				.findAll(PageRequest.of(query.page(), query.size(), Sort.by("id").ascending()))
				.map(mapper::toDomain);

		return new PageResult<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
	}

	@Override
	public boolean deleteById(final UUID id) {
		return jpaRepository.deleteVehicleById(id) > 0;
	}
}
