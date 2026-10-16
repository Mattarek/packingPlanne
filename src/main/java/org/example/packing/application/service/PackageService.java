package org.example.packing.application.service;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.port.IdGenerator;
import org.example.packing.application.port.PackageCreatedPublisher;
import org.example.packing.application.port.PackageRepository;
import org.example.packing.domain.exception.PackageNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Weight;
import org.example.packing.domain.policy.PackageAcceptancePolicy;
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
	private final PackageAcceptancePolicy acceptancePolicy;
	private final PackageCreatedPublisher packageCreatedPublisher;
	private final IdGenerator idGenerator;

	public PackageService(
			final PackageRepository packageRepository,
			final PackageAcceptancePolicy acceptancePolicy,
			final PackageCreatedPublisher packageCreatedPublisher,
			final IdGenerator idGenerator
	) {
		this.packageRepository = packageRepository;
		this.acceptancePolicy = acceptancePolicy;
		this.packageCreatedPublisher = packageCreatedPublisher;
		this.idGenerator = idGenerator;
	}

	@Transactional
	public List<Package> createPackages(final List<CreatePackageCommand> commands) {
		final List<Package> packages = commands.stream()
				.map(this::toPackage)
				.toList();

		packages.forEach(acceptancePolicy::validate);

		final List<Package> saved = packageRepository.saveAll(packages);

		packageCreatedPublisher.publish(saved);

		return saved;
	}

	private Package toPackage(final CreatePackageCommand command) {
		return new Package(
				idGenerator.newId(),
				new Dimensions(command.length(), command.width(), command.height()),
				new Weight(command.weight()),
				command.category(),
				command.fragility()
		);
	}

	@Transactional(readOnly = true)
	public Page<Package> getPackages(final int page, final int size) {
		final Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.by("id").ascending()
		);

		return packageRepository.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public Package getPackage(final UUID id) {
		return packageRepository.findById(id)
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
