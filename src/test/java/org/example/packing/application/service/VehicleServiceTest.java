package org.example.packing.application.service;

import org.example.packing.application.command.CreateVehicleCommand;
import org.example.packing.application.port.PageQuery;
import org.example.packing.application.port.PageResult;
import org.example.packing.application.port.out.VehicleRepository;
import org.example.packing.domain.exception.VehicleNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
	void shouldReturnRequestedPage() {
		// given
		final PageResult<Vehicle> page = new PageResult<>(List.of(existingVehicle), 0, 10, 1);
		when(vehicleRepository.findPage(new PageQuery(0, 10))).thenReturn(page);

		// when
		final PageResult<Vehicle> result = vehicleService.getVehicles(0, 10);

		// then
		assertThat(result).isEqualTo(page);
		verify(vehicleRepository).findPage(new PageQuery(0, 10));
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
		when(vehicleRepository.deleteById(vehicleId)).thenReturn(true);

		// when
		vehicleService.deleteVehicle(vehicleId);

		// then
		verify(vehicleRepository).deleteById(vehicleId);

		verifyNoMoreInteractions(vehicleRepository);
	}

	@Test
	void shouldThrowExceptionWhenVehicleDoesNotExistWhileDeletingVehicle() {
		// given
		when(vehicleRepository.deleteById(vehicleId)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> vehicleService.deleteVehicle(vehicleId))
				.isInstanceOf(VehicleNotFoundException.class);

		verify(vehicleRepository).deleteById(vehicleId);

		verifyNoMoreInteractions(vehicleRepository);
	}
}
