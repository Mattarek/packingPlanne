package org.example.packing.application.service;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.port.PageQuery;
import org.example.packing.application.port.PageResult;
import org.example.packing.application.port.out.IdGenerator;
import org.example.packing.application.port.out.PackageCreatedPublisher;
import org.example.packing.application.port.out.PackageRepository;
import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.exception.PackageNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.domain.model.Weight;
import org.example.packing.domain.policy.MaxDimensionsRule;
import org.example.packing.domain.policy.MaxWeightRule;
import org.example.packing.domain.policy.PackageAcceptancePolicy;
import org.example.packing.domain.policy.TransportableCategoryRule;
import org.example.packing.domain.policy.UltraFragileRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

	private static final UUID GENERATED_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	@Mock
	private PackageRepository packageRepository;

	@Mock
	private PackageCreatedPublisher packageCreatedPublisher;

	private final IdGenerator idGenerator = () -> GENERATED_ID;

	private final PackageAcceptancePolicy acceptancePolicy = new PackageAcceptancePolicy(List.of(
			new MaxDimensionsRule(new Dimensions(120.0, 80.0, 80.0)),
			new MaxWeightRule(new Weight(30.0)),
			new TransportableCategoryRule(),
			new UltraFragileRule()
	));

	private PackageService packageService;

	private UUID packageId;
	private Package existingPackage;

	@BeforeEach
	void setUp() {
		packageService = new PackageService(
				packageRepository,
				acceptancePolicy,
				packageCreatedPublisher,
				idGenerator
		);

		packageId = UUID.randomUUID();
		existingPackage = new Package(packageId, new Dimensions(10.0, 20.0, 30.0), new Weight(5.0));
	}

	@Test
	void shouldCreatePackages() {
		// given
		final CreatePackageCommand command = command(10.0, 20.0, 30.0, 5.0, ProductCategory.FOOD, FragilityLevel.FRAGILE);
		final Package expected = new Package(
				GENERATED_ID,
				new Dimensions(10.0, 20.0, 30.0),
				new Weight(5.0),
				ProductCategory.FOOD,
				FragilityLevel.FRAGILE
		);

		when(packageRepository.saveAll(List.of(expected))).thenReturn(List.of(expected));

		// when
		final List<Package> result = packageService.createPackages(List.of(command));

		// then
		assertThat(result).containsExactly(expected);

		verify(packageRepository).saveAll(List.of(expected));
		verify(packageCreatedPublisher).publish(List.of(expected));

		verifyNoMoreInteractions(packageRepository, packageCreatedPublisher);
	}

	@Test
	void shouldPublishWhatTheRepositorySaved() {
		// given
		final Package saved = new Package(UUID.randomUUID(), new Dimensions(1.0, 1.0, 1.0), new Weight(1.0));

		when(packageRepository.saveAll(anyList())).thenReturn(List.of(saved));

		// when
		packageService.createPackages(List.of(acceptableCommand()));

		// then
		final ArgumentCaptor<List<Package>> published = ArgumentCaptor.captor();
		verify(packageCreatedPublisher).publish(published.capture());
		assertThat(published.getValue()).containsExactly(saved);
	}

	@Test
	void shouldRejectPackageExceedingMaxAcceptedWeightWithoutTouchingRepository() {
		// when & then
		assertThatThrownBy(() -> packageService.createPackages(List.of(
				command(10.0, 20.0, 30.0, 31.0, ProductCategory.STANDARD, FragilityLevel.STANDARD)
		))).isInstanceOf(PackageNotAcceptedException.class);

		verifyNoInteractions(packageRepository, packageCreatedPublisher);
	}

	@Test
	void shouldRejectPackageExceedingMaxAcceptedDimensionsWithoutTouchingRepository() {
		// when & then
		assertThatThrownBy(() -> packageService.createPackages(List.of(
				command(500.0, 20.0, 30.0, 5.0, ProductCategory.STANDARD, FragilityLevel.STANDARD)
		))).isInstanceOf(PackageNotAcceptedException.class);

		verifyNoInteractions(packageRepository, packageCreatedPublisher);
	}

	@Test
	void shouldRejectNonTransportableProductCategoryWithoutTouchingRepository() {
		// when & then
		assertThatThrownBy(() -> packageService.createPackages(List.of(
				command(10.0, 20.0, 30.0, 5.0, ProductCategory.HAZARDOUS_MATERIAL, FragilityLevel.STANDARD)
		))).isInstanceOf(PackageNotAcceptedException.class);

		verifyNoInteractions(packageRepository, packageCreatedPublisher);
	}

	@Test
	void shouldRejectUltraFragilePackageWithoutTouchingRepository() {
		// when & then
		assertThatThrownBy(() -> packageService.createPackages(List.of(
				command(10.0, 20.0, 30.0, 5.0, ProductCategory.STANDARD, FragilityLevel.ULTRA_FRAGILE)
		))).isInstanceOf(PackageNotAcceptedException.class);

		verifyNoInteractions(packageRepository, packageCreatedPublisher);
	}

	@Test
	void shouldUseTheInjectedPolicyInsteadOfAHardCodedOne() {
		// given
		final PackageService permissiveService = new PackageService(
				packageRepository,
				new PackageAcceptancePolicy(List.of()),
				packageCreatedPublisher,
				idGenerator
		);
		when(packageRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		final List<Package> result = permissiveService.createPackages(List.of(
				command(10.0, 20.0, 30.0, 31.0, ProductCategory.STANDARD, FragilityLevel.STANDARD)
		));

		// then
		assertThat(result).hasSize(1);
	}

	@Test
	void shouldReturnRequestedPage() {
		// given
		final PageResult<Package> page = new PageResult<>(List.of(existingPackage), 0, 10, 1);
		when(packageRepository.findPage(new PageQuery(0, 10))).thenReturn(page);

		// when
		final PageResult<Package> result = packageService.getPackages(0, 10);

		// then
		assertThat(result).isEqualTo(page);
		verify(packageRepository).findPage(new PageQuery(0, 10));
		verifyNoMoreInteractions(packageRepository);
	}

	@Test
	void shouldReturnPackageById() {
		// given
		when(packageRepository.findById(packageId)).thenReturn(Optional.of(existingPackage));

		// when
		final Package result = packageService.getPackage(packageId);

		// then
		assertThat(result).isEqualTo(existingPackage);

		verify(packageRepository).findById(packageId);
		verifyNoMoreInteractions(packageRepository);
	}

	@Test
	void shouldThrowExceptionWhenPackageDoesNotExistWhileGettingPackage() {
		// given
		when(packageRepository.findById(packageId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> packageService.getPackage(packageId))
				.isInstanceOf(PackageNotFoundException.class);

		verify(packageRepository).findById(packageId);
		verifyNoMoreInteractions(packageRepository);
	}

	@Test
	void shouldDeletePackageById() {
		// given
		when(packageRepository.deleteById(packageId)).thenReturn(true);

		// when
		packageService.deletePackage(packageId);

		// then
		verify(packageRepository).deleteById(packageId);

		verifyNoMoreInteractions(packageRepository);
	}

	@Test
	void shouldThrowExceptionWhenPackageDoesNotExistWhileDeletingPackage() {
		// given
		when(packageRepository.deleteById(packageId)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> packageService.deletePackage(packageId))
				.isInstanceOf(PackageNotFoundException.class);

		verify(packageRepository).deleteById(packageId);

		verifyNoMoreInteractions(packageRepository);
	}

	private static CreatePackageCommand acceptableCommand() {
		return command(10.0, 20.0, 30.0, 5.0, ProductCategory.STANDARD, FragilityLevel.STANDARD);
	}

	private static CreatePackageCommand command(
			final double length,
			final double width,
			final double height,
			final double weight,
			final ProductCategory category,
			final FragilityLevel fragility
	) {
		return new CreatePackageCommand(length, width, height, weight, category, fragility);
	}
}
