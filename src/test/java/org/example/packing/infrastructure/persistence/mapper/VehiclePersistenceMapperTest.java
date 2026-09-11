package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
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

class VehiclePersistenceMapperTest {

	private VehiclePersistenceMapper mapper;

	private UUID vehicleId;
	private VehiclesEntity vehicleEntity;
	private VehicleRequest vehicleRequest;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(VehiclePersistenceMapper.class);

		vehicleId = UUID.randomUUID();

		vehicleEntity = new VehiclesEntity(
				vehicleId,
				"Test vehicle",
				100.0,
				50.0,
				40.0,
				1000.0
		);

		vehicleRequest = new VehicleRequest(
				"Request vehicle",
				120.0,
				60.0,
				45.0,
				1500.0
		);
	}

	@Test
	void shouldMapEntityToDomain() {
		// when
		final Vehicle result = mapper.toDomain(vehicleEntity);

		// then
		assertThat(result).isNotNull();

		assertThat(result.id()).isEqualTo(vehicleId);
		assertThat(result.name()).isEqualTo("Test vehicle");

		assertThat(result.cargoArea()).isEqualTo(new Dimensions(100.0, 50.0, 40.0));
		assertThat(result.maxPayload()).isEqualTo(new Weight(1000.0));
	}

	@Test
	void shouldMapEntityListToDomainList() {
		// given
		final VehiclesEntity secondEntity = new VehiclesEntity(
				UUID.randomUUID(),
				"Second vehicle",
				200.0,
				80.0,
				60.0,
				2000.0
		);

		// when
		final List<Vehicle> result = mapper.toDomainList(List.of(vehicleEntity, secondEntity));

		// then
		assertThat(result).hasSize(2);

		assertThat(result.get(0).id()).isEqualTo(vehicleEntity.getId());
		assertThat(result.get(0).name()).isEqualTo(vehicleEntity.getName());
		assertThat(result.get(0).cargoArea()).isEqualTo(new Dimensions(100.0, 50.0, 40.0));
		assertThat(result.get(0).maxPayload()).isEqualTo(new Weight(1000.0));

		assertThat(result.get(1).id()).isEqualTo(secondEntity.getId());
		assertThat(result.get(1).name()).isEqualTo(secondEntity.getName());
		assertThat(result.get(1).cargoArea()).isEqualTo(new Dimensions(200.0, 80.0, 60.0));
		assertThat(result.get(1).maxPayload()).isEqualTo(new Weight(2000.0));
	}

	@Test
	void shouldMapRequestToEntityAndIgnoreId() {
		// when
		final VehiclesEntity result = mapper.toEntity(vehicleRequest);

		// then
		assertThat(result).isNotNull();

		assertThat(result.getId()).isNull();
		assertThat(result.getName()).isEqualTo("Request vehicle");
		assertThat(result.getLength()).isEqualTo(120.0);
		assertThat(result.getWidth()).isEqualTo(60.0);
		assertThat(result.getHeight()).isEqualTo(45.0);
		assertThat(result.getMaxPayload()).isEqualTo(1500.0);
	}

	@Test
	void shouldMapRequestListToEntityList() {
		// given
		final VehicleRequest secondRequest = new VehicleRequest(
				"Second request vehicle",
				200.0,
				80.0,
				70.0,
				2500.0
		);

		// when
		final List<VehiclesEntity> result = mapper.toEntityList(List.of(vehicleRequest, secondRequest));

		// then
		assertThat(result).hasSize(2);

		assertThat(result.get(0).getId()).isNull();
		assertThat(result.get(0).getName()).isEqualTo("Request vehicle");
		assertThat(result.get(0).getLength()).isEqualTo(120.0);
		assertThat(result.get(0).getWidth()).isEqualTo(60.0);
		assertThat(result.get(0).getHeight()).isEqualTo(45.0);
		assertThat(result.get(0).getMaxPayload()).isEqualTo(1500.0);

		assertThat(result.get(1).getId()).isNull();
		assertThat(result.get(1).getName()).isEqualTo("Second request vehicle");
		assertThat(result.get(1).getLength()).isEqualTo(200.0);
		assertThat(result.get(1).getWidth()).isEqualTo(80.0);
		assertThat(result.get(1).getHeight()).isEqualTo(70.0);
		assertThat(result.get(1).getMaxPayload()).isEqualTo(2500.0);
	}

	@Test
	void shouldMapEntityToResponse() {
		// when
		final VehicleResponse result = mapper.toResponse(vehicleEntity);

		// then
		assertThat(result).isNotNull();

		assertThat(result.id()).isEqualTo(vehicleId);
		assertThat(result.name()).isEqualTo("Test vehicle");
		assertThat(result.length()).isEqualTo(100.0);
		assertThat(result.width()).isEqualTo(50.0);
		assertThat(result.height()).isEqualTo(40.0);
		assertThat(result.maxPayload()).isEqualTo(1000.0);
	}

	@Test
	void shouldMapEntityListToResponseList() {
		// given
		final VehiclesEntity secondEntity = new VehiclesEntity(
				UUID.randomUUID(),
				"Second vehicle",
				200.0,
				80.0,
				60.0,
				2000.0
		);

		// when
		final List<VehicleResponse> result = mapper.toResponseList(List.of(vehicleEntity, secondEntity));

		// then
		assertThat(result).hasSize(2);

		assertThat(result.get(0).id()).isEqualTo(vehicleEntity.getId());
		assertThat(result.get(0).name()).isEqualTo("Test vehicle");
		assertThat(result.get(0).length()).isEqualTo(100.0);
		assertThat(result.get(0).width()).isEqualTo(50.0);
		assertThat(result.get(0).height()).isEqualTo(40.0);
		assertThat(result.get(0).maxPayload()).isEqualTo(1000.0);

		assertThat(result.get(1).id()).isEqualTo(secondEntity.getId());
		assertThat(result.get(1).name()).isEqualTo("Second vehicle");
		assertThat(result.get(1).length()).isEqualTo(200.0);
		assertThat(result.get(1).width()).isEqualTo(80.0);
		assertThat(result.get(1).height()).isEqualTo(60.0);
		assertThat(result.get(1).maxPayload()).isEqualTo(2000.0);
	}

	@Test
	void shouldMapEntityToDimensions() {
		// when
		final Dimensions result = mapper.toDimensions(vehicleEntity);

		// then
		assertThat(result).isEqualTo(new Dimensions(100.0, 50.0, 40.0));
	}

	@Test
	void shouldMapDoubleToWeight() {
		// when
		final Weight result = mapper.toWeight(1234.5);

		// then
		assertThat(result).isEqualTo(new Weight(1234.5));
	}
}