package org.example.packing.application.service;

import org.example.packing.application.command.CreateVehicleCommand;
import org.example.packing.application.port.VehicleRepository;
import org.example.packing.domain.exception.VehicleNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

	private static final UUID GENERATED_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

	@Mock
	private VehicleRepository vehicleRepository;

	private VehicleService vehicleService;

	private UUID vehicleId;
	private Vehicle existingVehicle;

	@BeforeEach
	void setUp() {
		vehicleService = new VehicleService(vehicleRepository, () -> GENERATED_ID);

		vehicleId = UUID.randomUUID();
		existingVehicle = new Vehicle(vehicleId, "Van", new Dimensions(420.0, 180.0, 200.0), new Weight(1000.0));
	}

	@Test
	void shouldCreateVehicles() {
		// given
		final Vehicle expected = new Vehicle(
				GENERATED_ID,
				"Van",
				new Dimensions(420.0, 180.0, 200.0),
				new Weight(1000.0)
		);

		when(vehicleRepository.saveAll(List.of(expected))).thenReturn(List.of(expected));

		// when
		final List<Vehicle> result = vehicleService.createVehicles(List.of(
				new CreateVehicleCommand("Van", 420.0, 180.0, 200.0, 1000.0)
		));

		// then
		assertThat(result).containsExactly(expected);

		verify(vehicleRepository).saveAll(List.of(expected));
		verifyNoMoreInteractions(vehicleRepository);
	}

	@Test
	void shouldRejectVehicleWithNonPositiveDimensionWithoutTouchingRepository() {
		// when & then
		assertThatThrownBy(() -> vehicleService.createVehicles(List.of(
				new CreateVehicleCommand("Van", 420.0, 0.0, 200.0, 1000.0)
		))).isInstanceOf(IllegalArgumentException.class);

		verifyNoInteractions(vehicleRepository);
	}

	@Test
	void shouldRejectVehicleWithInfiniteDimensionWithoutTouchingRepository() {
		// when & then
		assertThatThrownBy(() -> vehicleService.createVehicles(List.of(
				new CreateVehicleCommand("Van", Double.POSITIVE_INFINITY, 180.0, 200.0, 1000.0)
		))).isInstanceOf(IllegalArgumentException.class);

		verifyNoInteractions(vehicleRepository);
	}

	@Test
	void shouldRejectVehicleWithNegativeMaxPayloadWithoutTouchingRepository() {
		// when & then
		assertThatThrownBy(() -> vehicleService.createVehicles(List.of(
				new CreateVehicleCommand("Van", 420.0, 180.0, 200.0, -1000.0)
		))).isInstanceOf(IllegalArgumentException.class);

		verifyNoInteractions(vehicleRepository);
	}

	@Test
	void shouldReturnPagedVehiclesSortedByIdAscending() {
		// given
		final int page = 0;
		final int size = 10;

		final Page<Vehicle> domainPage = new PageImpl<>(
				List.of(existingVehicle),
				PageRequest.of(page, size, Sort.by("id").ascending()),
				1
		);

		when(vehicleRepository.findAll(any(Pageable.class))).thenReturn(domainPage);

		final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

		// when
		final Page<Vehicle> result = vehicleService.getVehicles(page, size);

		// then
		assertThat(result.getContent()).containsExactly(existingVehicle);
		assertThat(result.getNumber()).isEqualTo(page);
		assertThat(result.getSize()).isEqualTo(size);
		assertThat(result.getTotalElements()).isEqualTo(1);

		verify(vehicleRepository).findAll(pageableCaptor.capture());

		final Pageable capturedPageable = pageableCaptor.getValue();

		assertThat(capturedPageable.getPageNumber()).isEqualTo(page);
		assertThat(capturedPageable.getPageSize()).isEqualTo(size);
		assertThat(capturedPageable.getSort()).isEqualTo(Sort.by("id").ascending());

		verifyNoMoreInteractions(vehicleRepository);
	}

	@Test
	void shouldReturnVehicleById() {
		// given
		when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(existingVehicle));

		// when
		final Vehicle result = vehicleService.getVehicle(vehicleId);

		// then
		assertThat(result).isEqualTo(existingVehicle);

		verify(vehicleRepository).findById(vehicleId);
		verifyNoMoreInteractions(vehicleRepository);
	}

	@Test
	void shouldThrowExceptionWhenVehicleDoesNotExistWhileGettingVehicle() {
		// given
		when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> vehicleService.getVehicle(vehicleId))
				.isInstanceOf(VehicleNotFoundException.class);

		verify(vehicleRepository).findById(vehicleId);
		verifyNoMoreInteractions(vehicleRepository);
	}

	@Test
	void shouldDeleteVehicleById() {
		// given
		when(vehicleRepository.existsById(vehicleId)).thenReturn(true);

		// when
		vehicleService.deleteVehicle(vehicleId);

		// then
		verify(vehicleRepository).existsById(vehicleId);
		verify(vehicleRepository).deleteById(vehicleId);

		verifyNoMoreInteractions(vehicleRepository);
	}

	@Test
	void shouldThrowExceptionWhenVehicleDoesNotExistWhileDeletingVehicle() {
		// given
		when(vehicleRepository.existsById(vehicleId)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> vehicleService.deleteVehicle(vehicleId))
				.isInstanceOf(VehicleNotFoundException.class);

		verify(vehicleRepository).existsById(vehicleId);
		verify(vehicleRepository, never()).deleteById(any());

		verifyNoMoreInteractions(vehicleRepository);
	}
}
