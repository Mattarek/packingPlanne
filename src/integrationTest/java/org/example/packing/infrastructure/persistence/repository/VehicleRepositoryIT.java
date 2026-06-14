package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.example.packing.integration.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VehicleRepositoryIT extends AbstractPostgresIntegrationTest {

	private final VehicleRepository vehicleRepository;

	public VehicleRepositoryIT(final VehicleRepository vehicleRepository) {
		this.vehicleRepository = vehicleRepository;
	}

	@Test
	void shouldSaveAndFindVehicleUsingPostgresContainer() {
		// given
		final VehiclesEntity entity = new VehiclesEntity(
				null,
				"Truck 1",
				100.0,
				50.0,
				40.0,
				1000.0
		);

		// when
		final VehiclesEntity saved = vehicleRepository.saveAndFlush(entity);

		// then
		assertThat(saved.getId()).isNotNull();

		final VehiclesEntity found = vehicleRepository.findById(saved.getId())
				.orElseThrow();

		assertThat(found.getId()).isEqualTo(saved.getId());
		assertThat(found.getName()).isEqualTo("Truck 1");
		assertThat(found.getLength()).isEqualTo(100.0);
		assertThat(found.getWidth()).isEqualTo(50.0);
		assertThat(found.getHeight()).isEqualTo(40.0);
		assertThat(found.getMaxPayload()).isEqualTo(1000.0);
	}

	@Test
	void shouldDeleteVehicleUsingPostgresContainer() {
		// given
		final VehiclesEntity entity = new VehiclesEntity(
				null,
				"Truck 1",
				100.0,
				50.0,
				40.0,
				1000.0
		);

		final VehiclesEntity saved = vehicleRepository.saveAndFlush(entity);

		// when
		vehicleRepository.deleteById(saved.getId());
		vehicleRepository.flush();

		// then
		assertThat(vehicleRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	void shouldFindAllSavedVehiclesUsingPostgresContainer() {
		// given
		final VehiclesEntity first = new VehiclesEntity(
				null,
				"Small truck",
				100.0,
				50.0,
				40.0,
				1000.0
		);

		final VehiclesEntity second = new VehiclesEntity(
				null,
				"Big truck",
				200.0,
				100.0,
				80.0,
				3000.0
		);

		vehicleRepository.saveAndFlush(first);
		vehicleRepository.saveAndFlush(second);

		// when
		final var result = vehicleRepository.findAll();

		// then
		assertThat(result)
				.hasSize(2)
				.extracting(VehiclesEntity::getName)
				.containsExactlyInAnyOrder("Small truck", "Big truck");
	}

	@Test
	void shouldGenerateDifferentIdsForDifferentVehicles() {
		// given
		final VehiclesEntity first = new VehiclesEntity(
				null,
				"Small truck",
				100.0,
				50.0,
				40.0,
				1000.0
		);

		final VehiclesEntity second = new VehiclesEntity(
				null,
				"Big truck",
				200.0,
				100.0,
				80.0,
				3000.0
		);

		// when
		final VehiclesEntity savedFirst = vehicleRepository.saveAndFlush(first);
		final VehiclesEntity savedSecond = vehicleRepository.saveAndFlush(second);

		// then
		assertThat(savedFirst.getId()).isNotNull();
		assertThat(savedSecond.getId()).isNotNull();
		assertThat(savedFirst.getId()).isNotEqualTo(savedSecond.getId());
	}

	@Test
	void shouldReturnEmptyOptionalWhenVehicleDoesNotExist() {
		// given
		final UUID nonExistingId = UUID.randomUUID();

		// when
		final var result = vehicleRepository.findById(nonExistingId);

		// then
		assertThat(result).isEmpty();
	}
}