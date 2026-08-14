package org.example.packing.application.service;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.exception.PackageNotFoundException;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.kafka.producer.PackageCreatedEventPublisher;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.example.packing.infrastructure.persistence.mapper.PackagePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

	@Mock
	private PackageRepository packageRepository;

	@Mock
	private PackagePersistenceMapper packageMapper;

	@Mock
	private PackageCreatedEventPublisher packageCreatedEventPublisher;

	@InjectMocks
	private PackageService packageService;

	private UUID packageId;

	private PackageRequest packageRequest;
	private PackageResponse packageResponse;
	private PackageEntity packageEntity;

	private List<PackageRequest> packageRequests;
	private List<PackageEntity> packageEntities;
	private List<PackageResponse> packageResponses;

	@BeforeEach
	void setUp() {
		packageId = UUID.randomUUID();

		packageRequest = mock(PackageRequest.class);
		packageResponse = mock(PackageResponse.class);
		packageEntity = mock(PackageEntity.class);

		packageRequests = List.of(packageRequest);
		packageEntities = List.of(packageEntity);
		packageResponses = List.of(packageResponse);
	}

	@Test
	void shouldCreatePackages() {
		// given
		stubAcceptablePackageRequest();

		when(packageMapper.toEntityList(packageRequests))
				.thenReturn(packageEntities);

		when(packageRepository.saveAll(packageEntities))
				.thenReturn(packageEntities);

		when(packageMapper.toResponseList(packageEntities))
				.thenReturn(packageResponses);

		// when
		final List<PackageResponse> result = packageService.createPackages(packageRequests);

		// then
		assertThat(result).isEqualTo(packageResponses);

		verify(packageMapper).toEntityList(packageRequests);
		verify(packageRepository).saveAll(packageEntities);
		verify(packageMapper).toResponseList(packageEntities);
		verify(packageCreatedEventPublisher).publish(packageResponses);

		verifyNoMoreInteractions(packageMapper, packageRepository, packageCreatedEventPublisher);
	}

	@Test
	void shouldRejectPackageExceedingMaxAcceptedWeightWithoutTouchingRepository() {
		// given
		stubPackageRequest(10.0, 20.0, 30.0, 31.0, ProductCategory.STANDARD, FragilityLevel.STANDARD);

		// when & then
		assertThatThrownBy(() -> packageService.createPackages(packageRequests))
				.isInstanceOf(PackageNotAcceptedException.class);

		verifyNoInteractions(packageMapper, packageRepository, packageCreatedEventPublisher);
	}

	@Test
	void shouldRejectPackageExceedingMaxAcceptedDimensionsWithoutTouchingRepository() {
		// given
		stubPackageRequest(500.0, 20.0, 30.0, 5.0, ProductCategory.STANDARD, FragilityLevel.STANDARD);

		// when & then
		assertThatThrownBy(() -> packageService.createPackages(packageRequests))
				.isInstanceOf(PackageNotAcceptedException.class);

		verifyNoInteractions(packageMapper, packageRepository, packageCreatedEventPublisher);
	}

	@Test
	void shouldRejectNonTransportableProductCategoryWithoutTouchingRepository() {
		// given
		stubPackageRequest(10.0, 20.0, 30.0, 5.0, ProductCategory.HAZARDOUS_MATERIAL, FragilityLevel.STANDARD);

		// when & then
		assertThatThrownBy(() -> packageService.createPackages(packageRequests))
				.isInstanceOf(PackageNotAcceptedException.class);

		verifyNoInteractions(packageMapper, packageRepository, packageCreatedEventPublisher);
	}

	@Test
	void shouldRejectUltraFragilePackageWithoutTouchingRepository() {
		// given
		stubPackageRequest(10.0, 20.0, 30.0, 5.0, ProductCategory.STANDARD, FragilityLevel.ULTRA_FRAGILE);

		// when & then
		assertThatThrownBy(() -> packageService.createPackages(packageRequests))
				.isInstanceOf(PackageNotAcceptedException.class);

		verifyNoInteractions(packageMapper, packageRepository, packageCreatedEventPublisher);
	}

	private void stubAcceptablePackageRequest() {
		stubPackageRequest(10.0, 20.0, 30.0, 5.0, ProductCategory.STANDARD, FragilityLevel.STANDARD);
	}

	private void stubPackageRequest(
			final double length,
			final double width,
			final double height,
			final double weight,
			final ProductCategory category,
			final FragilityLevel fragility
	) {
		when(packageRequest.length()).thenReturn(length);
		when(packageRequest.width()).thenReturn(width);
		when(packageRequest.height()).thenReturn(height);
		when(packageRequest.weight()).thenReturn(weight);
		when(packageRequest.category()).thenReturn(category);
		when(packageRequest.fragility()).thenReturn(fragility);
	}

	@Test
	void shouldReturnPagedPackagesSortedByIdAscending() {
		// given
		final int page = 0;
		final int size = 10;

		final Page<PackageEntity> entityPage = new PageImpl<>(
				packageEntities,
				PageRequest.of(page, size, Sort.by("id").ascending()),
				packageEntities.size()
		);

		when(packageRepository.findAll(any(Pageable.class)))
				.thenReturn(entityPage);

		when(packageMapper.toResponse(packageEntity))
				.thenReturn(packageResponse);

		final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

		// when
		final Page<PackageResponse> result = packageService.getPackages(page, size);

		// then
		assertThat(result.getContent()).containsExactly(packageResponse);
		assertThat(result.getNumber()).isEqualTo(page);
		assertThat(result.getSize()).isEqualTo(size);
		assertThat(result.getTotalElements()).isEqualTo(1);

		verify(packageRepository).findAll(pageableCaptor.capture());

		final Pageable capturedPageable = pageableCaptor.getValue();

		assertThat(capturedPageable.getPageNumber()).isEqualTo(page);
		assertThat(capturedPageable.getPageSize()).isEqualTo(size);
		assertThat(capturedPageable.getSort())
				.isEqualTo(Sort.by("id").ascending());

		verify(packageMapper).toResponse(packageEntity);

		verifyNoMoreInteractions(packageRepository, packageMapper);
	}

	@Test
	void shouldReturnPackageById() {
		// given
		when(packageRepository.findById(packageId))
				.thenReturn(Optional.of(packageEntity));

		when(packageMapper.toResponse(packageEntity))
				.thenReturn(packageResponse);

		// when
		final PackageResponse result = packageService.getPackage(packageId);

		// then
		assertThat(result).isEqualTo(packageResponse);

		verify(packageRepository).findById(packageId);
		verify(packageMapper).toResponse(packageEntity);

		verifyNoMoreInteractions(packageRepository, packageMapper);
	}

	@Test
	void shouldThrowExceptionWhenPackageDoesNotExistWhileGettingPackage() {
		// given
		when(packageRepository.findById(packageId))
				.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> packageService.getPackage(packageId))
				.isInstanceOf(PackageNotFoundException.class);

		verify(packageRepository).findById(packageId);

		verifyNoInteractions(packageMapper);
		verifyNoMoreInteractions(packageRepository);
	}

	@Test
	void shouldDeletePackageById() {
		// given
		when(packageRepository.existsById(packageId))
				.thenReturn(true);

		// when
		packageService.deletePackage(packageId);

		// then
		verify(packageRepository).existsById(packageId);
		verify(packageRepository).deleteById(packageId);

		verifyNoInteractions(packageMapper);
		verifyNoMoreInteractions(packageRepository);
	}

	@Test
	void shouldThrowExceptionWhenPackageDoesNotExistWhileDeletingPackage() {
		// given
		when(packageRepository.existsById(packageId))
				.thenReturn(false);

		// when & then
		assertThatThrownBy(() -> packageService.deletePackage(packageId))
				.isInstanceOf(PackageNotFoundException.class);

		verify(packageRepository).existsById(packageId);
		verify(packageRepository, never()).deleteById(any());

		verifyNoInteractions(packageMapper);
		verifyNoMoreInteractions(packageRepository);
	}
}