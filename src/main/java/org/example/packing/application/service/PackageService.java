package org.example.packing.application.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.example.packing.infrastructure.persistence.mapper.PackagePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PackageService {

	private final PackageRepository packageRepository;
	private final PackagePersistenceMapper packageMapper;

	public PackageService(
			final PackageRepository packageRepository,
			final PackagePersistenceMapper packageMapper
	) {
		this.packageRepository = packageRepository;
		this.packageMapper = packageMapper;
	}

	@Transactional
	public PackageResponse createPackage(final PackageRequest request) {
		final PackageEntity entity = packageMapper.toEntity(request);
		final PackageEntity saved = packageRepository.save(entity);

		return packageMapper.toResponse(saved);
	}

	@Transactional
	public List<PackageResponse> createPackages(final List<PackageRequest> requests) {
		final List<PackageEntity> entities = packageMapper.toEntityList(requests);
		final List<PackageEntity> savedEntities = packageRepository.saveAll(entities);

		return packageMapper.toResponseList(savedEntities);
	}

	@Transactional(readOnly = true)
	public Page<PackageResponse> getPackages(final int page, final int size) {
		final Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.by("id").ascending()
		);

		return packageRepository.findAll(pageable)
				.map(packageMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public PackageResponse getPackage(final UUID id) {
		return packageRepository.findById(id.toString())
				.map(packageMapper::toResponse)
				.orElseThrow(() -> new EntityNotFoundException("Package not found: " + id));
	}

	@Transactional
	public void deletePackage(final UUID id) {
		final String packageId = id.toString();

		if (!packageRepository.existsById(packageId)) {
			throw new EntityNotFoundException("Package not found: " + packageId);
		}

		packageRepository.deleteById(packageId);
	}
}