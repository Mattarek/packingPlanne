package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.application.port.PackageRepository;
import org.example.packing.domain.model.Package;
import org.example.packing.infrastructure.persistence.mapper.PackageEntityMapper;
import org.example.packing.infrastructure.persistence.repository.PackageJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements the application's {@link PackageRepository} port with Spring Data JPA.
 * The JPA entity and the full {@code JpaRepository} API stay inside this package.
 */
@Component
public class JpaPackageRepositoryAdapter implements PackageRepository {

	private final PackageJpaRepository jpaRepository;
	private final PackageEntityMapper mapper;

	public JpaPackageRepositoryAdapter(
			final PackageJpaRepository jpaRepository,
			final PackageEntityMapper mapper
	) {
		this.jpaRepository = jpaRepository;
		this.mapper = mapper;
	}

	@Override
	public List<Package> saveAll(final List<Package> packages) {
		return mapper.toDomainList(jpaRepository.saveAll(mapper.toEntityList(packages)));
	}

	@Override
	public Optional<Package> findById(final UUID id) {
		return jpaRepository.findById(id).map(mapper::toDomain);
	}

	@Override
	public Page<Package> findAll(final Pageable pageable) {
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
