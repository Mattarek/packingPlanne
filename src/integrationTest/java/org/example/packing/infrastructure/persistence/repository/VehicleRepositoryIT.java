package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.persistence.entity.VehicleEntity;
import org.example.packing.integration.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VehicleRepositoryIT extends AbstractPostgresIntegrationTest {

	private final VehicleJpaRepository vehicleRepository;

	public VehicleRepositoryIT(final VehicleJpaRepository vehicleRepository) {
		this.vehicleRepository = vehicleRepository;
	}

	@Test
	void shouldSaveAndFindVehicleUsingPostgresContainer() {
		// given
		final VehicleEntity entity = new VehicleEntity(
				UUID.randomUUID(),
				"Truck 1",
				100.0,
				50.0,
				40.0,
				1000.0
		);

		// when
		final VehicleEntity saved = vehicleRepository.saveAndFlush(entity);

		// then
		assertThat(saved.getId()).isNotNull();

		final VehicleEntity found = vehicleRepository.findById(saved.getId())
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
		final VehicleEntity entity = new VehicleEntity(
				UUID.randomUUID(),
				"Truck 1",
				100.0,
				50.0,
				40.0,
				1000.0
		);

		final VehicleEntity saved = vehicleRepository.saveAndFlush(entity);

		// when
		vehicleRepository.deleteById(saved.getId());
		vehicleRepository.flush();

		// then
		assertThat(vehicleRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	void shouldFindAllSavedVehiclesUsingPostgresContainer() {
		// given
		final VehicleEntity first = new VehicleEntity(
				UUID.randomUUID(),
				"Small truck",
				100.0,
				50.0,
				40.0,
				1000.0
		);

		final VehicleEntity second = new VehicleEntity(
				UUID.randomUUID(),
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
				.extracting(VehicleEntity::getName)
				.containsExactlyInAnyOrder("Small truck", "Big truck");
	}

	@Test
	void shouldGenerateDifferentIdsForDifferentVehicles() {
		// given
		final VehicleEntity first = new VehicleEntity(
				UUID.randomUUID(),
				"Small truck",
				100.0,
				50.0,
				40.0,
				1000.0
		);

		final VehicleEntity second = new VehicleEntity(
				UUID.randomUUID(),
				"Big truck",
				200.0,
				100.0,
				80.0,
				3000.0
		);

		// when
		final VehicleEntity savedFirst = vehicleRepository.saveAndFlush(first);
		final VehicleEntity savedSecond = vehicleRepository.saveAndFlush(second);

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