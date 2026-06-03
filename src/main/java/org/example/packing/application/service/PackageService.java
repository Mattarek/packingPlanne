package org.example.packing.application.service;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.example.packing.infrastructure.persistence.mapper.PackagePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
	public void createPackage(final PackageRequest request) {
		final PackageEntity entity = packageMapper.toEntity(request);
		packageRepository.save(entity);
	}

	@Transactional
	public void createPackages(final List<PackageRequest> requests) {
		final List<PackageEntity> entities = requests.stream().map(packageMapper::toEntity).toList();
		packageRepository.saveAll(entities);
	}

	@Transactional(readOnly = true)
	public List<PackageResponse> getPackages() {
		return packageMapper.toResponseList(packageRepository.findAll());
	}

	@Transactional(readOnly = true)
	public PackageResponse getPackage(final String id) {
		return packageRepository.findById(id)
				.map(packageMapper::toResponse)
				.orElseThrow(() -> new IllegalArgumentException("Package not found: " + id));
	}

	@Transactional
	public void deletePackage(final String id) {
		if (!packageRepository.existsById(id)) {
			throw new IllegalArgumentException("Package not found: " + id);
		}

		packageRepository.deleteById(id);
	}

	@Transactional
	public void deleteAllPackages() {
		packageRepository.deleteAll();
	}
}