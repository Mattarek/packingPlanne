package org.example.packing.application.service;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.domain.exception.VehicleNotFoundException;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.example.packing.infrastructure.persistence.mapper.VehiclePersistenceMapper;
import org.example.packing.infrastructure.persistence.repository.VehicleRepository;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

	@Mock
	private VehicleRepository vehicleRepository;

	@Mock
	private VehiclePersistenceMapper vehicleMapper;

	@InjectMocks
	private VehicleService vehicleService;

	private UUID vehicleId;

	private VehicleRequest vehicleRequest;
	private VehicleResponse vehicleResponse;
	private VehiclesEntity vehicleEntity;

	private List<VehicleRequest> vehicleRequests;
	private List<VehiclesEntity> vehicleEntities;
	private List<VehicleResponse> vehicleResponses;

	@BeforeEach
	void setUp() {
		vehicleId = UUID.randomUUID();

		vehicleRequest = mock(VehicleRequest.class);
		vehicleResponse = mock(VehicleResponse.class);
		vehicleEntity = mock(VehiclesEntity.class);

		vehicleRequests = List.of(vehicleRequest);
		vehicleEntities = List.of(vehicleEntity);
		vehicleResponses = List.of(vehicleResponse);
	}

	@Test
	void shouldCreateVehicles() {
		// given
		stubValidVehicleRequest();

		when(vehicleMapper.toEntityList(vehicleRequests))
				.thenReturn(vehicleEntities);

		when(vehicleRepository.saveAll(vehicleEntities))
				.thenReturn(vehicleEntities);

		when(vehicleMapper.toResponseList(vehicleEntities))
				.thenReturn(vehicleResponses);

		// when
		final List<VehicleResponse> result = vehicleService.createVehicles(vehicleRequests);

		// then
		assertThat(result).isEqualTo(vehicleResponses);

		verify(vehicleMapper).toEntityList(vehicleRequests);
		verify(vehicleRepository).saveAll(vehicleEntities);
		verify(vehicleMapper).toResponseList(vehicleEntities);

		verifyNoMoreInteractions(vehicleMapper, vehicleRepository);
	}

	@Test
	void shouldRejectVehicleWithNonPositiveDimensionWithoutTouchingRepository() {
		// given: Dimensions checks length, then width, then height —
		// only stub what's actually evaluated before it short-circuits.
		when(vehicleRequest.length()).thenReturn(420.0);
		when(vehicleRequest.width()).thenReturn(0.0);

		// when & then
		assertThatThrownBy(() -> vehicleService.createVehicles(vehicleRequests))
				.isInstanceOf(IllegalArgumentException.class);

		verifyNoInteractions(vehicleMapper, vehicleRepository);
	}

	@Test
	void shouldRejectVehicleWithInfiniteDimensionWithoutTouchingRepository() {
		// given: length fails first, so Dimensions never reaches width/height.
		when(vehicleRequest.length()).thenReturn(Double.POSITIVE_INFINITY);

		// when & then
		assertThatThrownBy(() -> vehicleService.createVehicles(vehicleRequests))
				.isInstanceOf(IllegalArgumentException.class);

		verifyNoInteractions(vehicleMapper, vehicleRepository);
	}

	@Test
	void shouldRejectVehicleWithNegativeMaxPayloadWithoutTouchingRepository() {
		// given
		stubVehicleRequest(420.0, 180.0, 200.0, -1000.0);

		// when & then
		assertThatThrownBy(() -> vehicleService.createVehicles(vehicleRequests))
				.isInstanceOf(IllegalArgumentException.class);

		verifyNoInteractions(vehicleMapper, vehicleRepository);
	}

	private void stubValidVehicleRequest() {
		stubVehicleRequest(420.0, 180.0, 200.0, 1000.0);
	}

	private void stubVehicleRequest(
			final double length,
			final double width,
			final double height,
			final double maxPayload
	) {
		when(vehicleRequest.length()).thenReturn(length);
		when(vehicleRequest.width()).thenReturn(width);
		when(vehicleRequest.height()).thenReturn(height);
		when(vehicleRequest.maxPayload()).thenReturn(maxPayload);
	}

	@Test
	void shouldReturnPagedVehiclesSortedByIdAscending() {
		// given
		final int page = 0;
		final int size = 10;

		final Page<VehiclesEntity> entityPage = new PageImpl<>(
				vehicleEntities,
				PageRequest.of(page, size, Sort.by("id").ascending()),
				vehicleEntities.size()
		);

		when(vehicleRepository.findAll(any(Pageable.class)))
				.thenReturn(entityPage);

		when(vehicleMapper.toResponse(vehicleEntity))
				.thenReturn(vehicleResponse);

		final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

		// when
		final Page<VehicleResponse> result = vehicleService.getVehicles(page, size);

		// then
		assertThat(result.getContent()).containsExactly(vehicleResponse);
		assertThat(result.getNumber()).isEqualTo(page);
		assertThat(result.getSize()).isEqualTo(size);
		assertThat(result.getTotalElements()).isEqualTo(1);

		verify(vehicleRepository).findAll(pageableCaptor.capture());

		final Pageable capturedPageable = pageableCaptor.getValue();

		assertThat(capturedPageable.getPageNumber()).isEqualTo(page);
		assertThat(capturedPageable.getPageSize()).isEqualTo(size);
		assertThat(capturedPageable.getSort())
				.isEqualTo(Sort.by("id").ascending());

		verify(vehicleMapper).toResponse(vehicleEntity);

		verifyNoMoreInteractions(vehicleRepository, vehicleMapper);
	}

	@Test
	void shouldReturnVehicleById() {
		// given
		when(vehicleRepository.findById(vehicleId))
				.thenReturn(Optional.of(vehicleEntity));

		when(vehicleMapper.toResponse(vehicleEntity))
				.thenReturn(vehicleResponse);

		// when
		final VehicleResponse result = vehicleService.getVehicle(vehicleId);

		// then
		assertThat(result).isEqualTo(vehicleResponse);

		verify(vehicleRepository).findById(vehicleId);
		verify(vehicleMapper).toResponse(vehicleEntity);

		verifyNoMoreInteractions(vehicleRepository, vehicleMapper);
	}

	@Test
	void shouldThrowExceptionWhenVehicleDoesNotExistWhileGettingVehicle() {
		// given
		when(vehicleRepository.findById(vehicleId))
				.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> vehicleService.getVehicle(vehicleId))
				.isInstanceOf(VehicleNotFoundException.class);

		verify(vehicleRepository).findById(vehicleId);

		verifyNoInteractions(vehicleMapper);
		verifyNoMoreInteractions(vehicleRepository);
	}

	@Test
	void shouldDeleteVehicleById() {
		// given
		when(vehicleRepository.existsById(vehicleId))
				.thenReturn(true);

		// when
		vehicleService.deleteVehicle(vehicleId);

		// then
		verify(vehicleRepository).existsById(vehicleId);
		verify(vehicleRepository).deleteById(vehicleId);

		verifyNoInteractions(vehicleMapper);
		verifyNoMoreInteractions(vehicleRepository);
	}

	@Test
	void shouldThrowExceptionWhenVehicleDoesNotExistWhileDeletingVehicle() {
		// given
		when(vehicleRepository.existsById(vehicleId))
				.thenReturn(false);

		// when & then
		assertThatThrownBy(() -> vehicleService.deleteVehicle(vehicleId))
				.isInstanceOf(VehicleNotFoundException.class);

		verify(vehicleRepository).existsById(vehicleId);
		verify(vehicleRepository, never()).deleteById(any());

		verifyNoInteractions(vehicleMapper);
		verifyNoMoreInteractions(vehicleRepository);
	}
}