package org.example.packing.application.service;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.domain.exception.PackageNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Weight;
import org.example.packing.domain.policy.PackageAcceptancePolicy;
import org.example.packing.infrastructure.kafka.producer.PackageCreatedEventPublisher;
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
	private final PackageCreatedEventPublisher packageCreatedEventPublisher;

	public PackageService(
			final PackageRepository packageRepository,
			final PackagePersistenceMapper packageMapper,
			final PackageCreatedEventPublisher packageCreatedEventPublisher
	) {
		this.packageRepository = packageRepository;
		this.packageMapper = packageMapper;
		this.packageCreatedEventPublisher = packageCreatedEventPublisher;
	}

	@Transactional
	public List<PackageResponse> createPackages(final List<PackageRequest> packages) {
		packages.forEach(this::validateAcceptance);

		final List<PackageEntity> entities = packageMapper.toEntityList(packages);
		final List<PackageEntity> savedEntities = packageRepository.saveAll(entities);
		final List<PackageResponse> responses = packageMapper.toResponseList(savedEntities);

		// Same transaction as the insert above — transactional outbox pattern:
		// the "package created" event either commits with the packages or not at all.
		packageCreatedEventPublisher.publish(responses);

		return responses;
	}

	/**
	 * Enforces company-wide acceptance rules (max size/weight, transportable
	 * product categories, no ultra-fragile items) before a package is ever
	 * persisted. See {@link PackageAcceptancePolicy}.
	 */
	private void validateAcceptance(final PackageRequest request) {
		PackageAcceptancePolicy.validate(
				new Dimensions(request.length(), request.width(), request.height()),
				new Weight(request.weight()),
				request.category(),
				request.fragility()
		);
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
		return packageRepository.findById(id)
				.map(packageMapper::toResponse)
				.orElseThrow(() -> new PackageNotFoundException(id));
	}

	@Transactional
	public void deletePackage(final UUID id) {
		if (!packageRepository.existsById(id)) {
			throw new PackageNotFoundException(id);
		}

		packageRepository.deleteById(id);
	}
}