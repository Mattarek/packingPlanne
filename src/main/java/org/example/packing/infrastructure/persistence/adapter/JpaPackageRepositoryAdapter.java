package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.application.port.PageQuery;
import org.example.packing.application.port.PageResult;
import org.example.packing.application.port.out.PackageRepository;
import org.example.packing.domain.model.Package;
import org.example.packing.infrastructure.persistence.mapper.PackageEntityMapper;
import org.example.packing.infrastructure.persistence.repository.PackageJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
	public PageResult<Package> findPage(final PageQuery query) {
		final Page<Package> page = jpaRepository
				.findAll(PageRequest.of(query.page(), query.size(), Sort.by("id").ascending()))
				.map(mapper::toDomain);

		return new PageResult<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
	}

	@Override
	public boolean deleteById(final UUID id) {
		return jpaRepository.deletePackageById(id) > 0;
	}
}
