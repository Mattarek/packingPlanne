package org.example.packing.application.service;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.port.PageQuery;
import org.example.packing.application.port.PageResult;
import org.example.packing.application.port.in.PackageUseCase;
import org.example.packing.application.port.out.IdGenerator;
import org.example.packing.application.port.out.PackageCreatedPublisher;
import org.example.packing.application.port.out.PackageRepository;
import org.example.packing.domain.exception.PackageNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Weight;
import org.example.packing.domain.policy.PackageAcceptancePolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PackageService implements PackageUseCase {

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

	@Override
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

	@Override
	@Transactional(readOnly = true)
	public PageResult<Package> getPackages(final int page, final int size) {
		return packageRepository.findPage(new PageQuery(page, size));
	}

	@Override
	@Transactional(readOnly = true)
	public Package getPackage(final UUID id) {
		return packageRepository.findById(id)
				.orElseThrow(() -> new PackageNotFoundException(id));
	}

	@Override
	@Transactional
	public void deletePackage(final UUID id) {
		if (!packageRepository.deleteById(id)) {
			throw new PackageNotFoundException(id);
		}
	}
}
