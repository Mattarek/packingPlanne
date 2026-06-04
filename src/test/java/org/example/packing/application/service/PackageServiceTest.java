package org.example.packing.application.service;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.example.packing.infrastructure.persistence.mapper.PackagePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

	@Mock
	private PackageRepository packageRepository;

	@Mock
	private PackagePersistenceMapper packageMapper;

	@InjectMocks
	private PackageService packageService;

	@Test
	void shouldCreatePackage() {
		final PackageRequest request = new PackageRequest("PKG-001", 90.0, 60.0, 50.0, 200.0);
		final PackageEntity entity = new PackageEntity("PKG-001", 90.0, 60.0, 50.0, 200.0);

		when(packageMapper.toEntity(request)).thenReturn(entity);

		packageService.createPackage(request);

		verify(packageMapper).toEntity(request);
		verify(packageRepository).save(entity);
	}

	@Test
	void shouldCreatePackages() {
		final PackageRequest first = new PackageRequest("PKG-001", 90.0, 60.0, 50.0, 200.0);
		final PackageRequest second = new PackageRequest("PKG-002", 40.0, 20.0, 10.0, 10.0);

		final PackageEntity firstEntity = new PackageEntity("PKG-001", 90.0, 60.0, 50.0, 200.0);
		final PackageEntity secondEntity = new PackageEntity("PKG-002", 40.0, 20.0, 10.0, 10.0);

		when(packageMapper.toEntity(first)).thenReturn(firstEntity);
		when(packageMapper.toEntity(second)).thenReturn(secondEntity);

		packageService.createPackages(List.of(first, second));

		verify(packageRepository).saveAll(List.of(firstEntity, secondEntity));
	}

	@Test
	void shouldGetPackages() {
		final PackageEntity entity = new PackageEntity("PKG-001", 90.0, 60.0, 50.0, 200.0);
		final PackageResponse response = new PackageResponse("PKG-001", 90.0, 60.0, 50.0, 200.0);

		when(packageRepository.findAll()).thenReturn(List.of(entity));
		when(packageMapper.toResponseList(List.of(entity))).thenReturn(List.of(response));

		final List<PackageResponse> result = packageService.getPackages();

		assertEquals(1, result.size());
		assertEquals("PKG-001", result.get(0).id());
		assertEquals(90.0, result.get(0).length());
		assertEquals(200.0, result.get(0).weight());
	}

	@Test
	void shouldGetPackageById() {
		final PackageEntity entity = new PackageEntity("PKG-001", 90.0, 60.0, 50.0, 200.0);
		final PackageResponse response = new PackageResponse("PKG-001", 90.0, 60.0, 50.0, 200.0);

		when(packageRepository.findById("PKG-001")).thenReturn(Optional.of(entity));
		when(packageMapper.toResponse(entity)).thenReturn(response);

		final PackageResponse result = packageService.getPackage("PKG-001");

		assertEquals("PKG-001", result.id());
	}

	@Test
	void shouldThrowWhenPackageDoesNotExist() {
		when(packageRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> packageService.getPackage("UNKNOWN"));
	}

	@Test
	void shouldDeletePackage() {
		when(packageRepository.existsById("PKG-001")).thenReturn(true);

		packageService.deletePackage("PKG-001");

		verify(packageRepository).deleteById("PKG-001");
	}

	@Test
	void shouldThrowWhenDeletingMissingPackage() {
		when(packageRepository.existsById("UNKNOWN")).thenReturn(false);

		assertThrows(IllegalArgumentException.class, () -> packageService.deletePackage("UNKNOWN"));

		verify(packageRepository, never()).deleteById("UNKNOWN");
	}

	@Test
	void shouldDeleteAllPackages() {
		packageService.deleteAllPackages();

		verify(packageRepository).deleteAll();
	}
}