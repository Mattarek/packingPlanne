package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleEntityMapperTest {

	private VehicleEntityMapper mapper;

	private UUID vehicleId;
	private VehiclesEntity vehicleEntity;
	private Vehicle vehicle;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(VehicleEntityMapper.class);

		vehicleId = UUID.randomUUID();

		vehicleEntity = new VehiclesEntity(vehicleId, "Test vehicle", 100.0, 50.0, 40.0, 1000.0);
		vehicle = new Vehicle(vehicleId, "Test vehicle", new Dimensions(100.0, 50.0, 40.0), new Weight(1000.0));
	}

	@Test
	void shouldMapEntityToDomain() {
		// when
		final Vehicle result = mapper.toDomain(vehicleEntity);

		// then
		assertThat(result).isEqualTo(vehicle);
	}

	@Test
	void shouldMapEntityListToDomainList() {
		// given
		final VehiclesEntity secondEntity = new VehiclesEntity(
				UUID.randomUUID(), "Second vehicle", 200.0, 80.0, 60.0, 2000.0
		);

		// when
		final List<Vehicle> result = mapper.toDomainList(List.of(vehicleEntity, secondEntity));

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isEqualTo(vehicle);
		assertThat(result.get(1).name()).isEqualTo("Second vehicle");
		assertThat(result.get(1).cargoArea()).isEqualTo(new Dimensions(200.0, 80.0, 60.0));
		assertThat(result.get(1).maxPayload()).isEqualTo(new Weight(2000.0));
	}

	@Test
	void shouldMapDomainToEntityKeepingTheDomainId() {
		// when
		final VehiclesEntity result = mapper.toEntity(vehicle);

		// then
		assertThat(result.getId()).isEqualTo(vehicleId);
		assertThat(result.getName()).isEqualTo("Test vehicle");
		assertThat(result.getLength()).isEqualTo(100.0);
		assertThat(result.getWidth()).isEqualTo(50.0);
		assertThat(result.getHeight()).isEqualTo(40.0);
		assertThat(result.getMaxPayload()).isEqualTo(1000.0);
		assertThat(result.isNew()).isTrue();
	}

	@Test
	void shouldRoundTripDomainThroughEntity() {
		// when
		final Vehicle result = mapper.toDomain(mapper.toEntity(vehicle));

		// then
		assertThat(result).isEqualTo(vehicle);
	}

	@Test
	void shouldMapEmptyLists() {
		// when & then
		assertThat(mapper.toDomainList(List.of())).isEmpty();
		assertThat(mapper.toEntityList(List.of())).isEmpty();
	}
}
