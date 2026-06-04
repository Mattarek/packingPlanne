package org.example.packing.application.service;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.example.packing.infrastructure.persistence.mapper.VehiclePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.VehicleRepository;
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
class VehicleServiceTest {

	@Mock
	private VehicleRepository vehicleRepository;

	@Mock
	private VehiclePersistenceMapper vehicleMapper;

	@InjectMocks
	private VehicleService vehicleService;

	@Test
	void shouldCreateVehicle() {
		final VehicleRequest request = new VehicleRequest(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		final VehiclesEntity entity = new VehiclesEntity(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		when(vehicleMapper.toEntity(request)).thenReturn(entity);

		vehicleService.createVehicle(request);

		verify(vehicleMapper).toEntity(request);
		verify(vehicleRepository).save(entity);
	}

	@Test
	void shouldCreateVehicles() {
		final VehicleRequest first = new VehicleRequest(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		final VehicleRequest second = new VehicleRequest(
				"TRUCK-001",
				"Iveco Daily 7t",
				620.0,
				220.0,
				230.0,
				3500.0
		);

		final VehiclesEntity firstEntity = new VehiclesEntity(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		final VehiclesEntity secondEntity = new VehiclesEntity(
				"TRUCK-001",
				"Iveco Daily 7t",
				620.0,
				220.0,
				230.0,
				3500.0
		);

		when(vehicleMapper.toEntityList(List.of(first, second)))
				.thenReturn(List.of(firstEntity, secondEntity));

		vehicleService.createVehicles(List.of(first, second));

		verify(vehicleRepository).saveAll(List.of(firstEntity, secondEntity));
	}

	@Test
	void shouldGetVehicles() {
		final VehiclesEntity entity = new VehiclesEntity(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		final VehicleResponse response = new VehicleResponse(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		when(vehicleRepository.findAll()).thenReturn(List.of(entity));
		when(vehicleMapper.toResponseList(List.of(entity))).thenReturn(List.of(response));

		final List<VehicleResponse> result = vehicleService.getVehicles();

		assertEquals(1, result.size());
		assertEquals("VAN-001", result.get(0).id());
		assertEquals(420.0, result.get(0).length());
		assertEquals(1000.0, result.get(0).maxPayload());
	}

	@Test
	void shouldGetVehicleById() {
		final VehiclesEntity entity = new VehiclesEntity(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		final VehicleResponse response = new VehicleResponse(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		when(vehicleRepository.findById("VAN-001")).thenReturn(Optional.of(entity));
		when(vehicleMapper.toResponse(entity)).thenReturn(response);

		final VehicleResponse result = vehicleService.getVehicle("VAN-001");

		assertEquals("VAN-001", result.id());
		assertEquals("Mercedes Sprinter", result.name());
		assertEquals(420.0, result.length());
	}

	@Test
	void shouldThrowWhenVehicleDoesNotExist() {
		when(vehicleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> vehicleService.getVehicle("UNKNOWN"));
	}

	@Test
	void shouldDeleteVehicle() {
		when(vehicleRepository.existsById("VAN-001")).thenReturn(true);

		vehicleService.deleteVehicle("VAN-001");

		verify(vehicleRepository).deleteById("VAN-001");
	}

	@Test
	void shouldThrowWhenDeletingMissingVehicle() {
		when(vehicleRepository.existsById("UNKNOWN")).thenReturn(false);

		assertThrows(IllegalArgumentException.class, () -> vehicleService.deleteVehicle("UNKNOWN"));

		verify(vehicleRepository, never()).deleteById("UNKNOWN");
	}

	@Test
	void shouldDeleteAllVehicles() {
		vehicleService.deleteAllVehicles();

		verify(vehicleRepository).deleteAll();
	}
}