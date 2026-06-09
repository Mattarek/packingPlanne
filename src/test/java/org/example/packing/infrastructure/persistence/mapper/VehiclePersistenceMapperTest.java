package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VehiclePersistenceMapperTest {

	private final VehiclePersistenceMapper mapper = new VehiclePersistenceMapperImpl();

	@Test
	void shouldMapRequestToEntity() {
		final VehicleRequest request = new VehicleRequest(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		final VehiclesEntity entity = mapper.toEntity(request);

		assertEquals("VAN-001", entity.getId());
		assertEquals("Mercedes Sprinter", entity.getName());
		assertEquals(420.0, entity.getLength());
		assertEquals(180.0, entity.getWidth());
		assertEquals(200.0, entity.getHeight());
		assertEquals(1000.0, entity.getMaxPayload());
	}

	@Test
	void shouldMapEntityToResponse() {
		final VehiclesEntity entity = new VehiclesEntity(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		final VehicleResponse response = mapper.toResponse(entity);

		assertEquals("VAN-001", response.id());
		assertEquals("Mercedes Sprinter", response.name());
		assertEquals(420.0, response.length());
		assertEquals(180.0, response.width());
		assertEquals(200.0, response.height());
		assertEquals(1000.0, response.maxPayload());
	}

	@Test
	void shouldMapEntityToDomain() {
		final VehiclesEntity entity = new VehiclesEntity(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		final Vehicle domain = mapper.toDomain(entity);

		assertEquals("VAN-001", domain.id());
		assertEquals("Mercedes Sprinter", domain.name());
		assertEquals(420.0, domain.cargoArea().length());
		assertEquals(180.0, domain.cargoArea().width());
		assertEquals(200.0, domain.cargoArea().height());
		assertEquals(1000.0, domain.maxPayload().kilograms());
	}

	@Test
	void shouldMapEntityListToResponseList() {
		final VehiclesEntity first = new VehiclesEntity(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		final VehiclesEntity second = new VehiclesEntity(
				"TRUCK-001",
				"Iveco Daily 7t",
				620.0,
				220.0,
				230.0,
				3500.0
		);

		final List<VehicleResponse> responses = mapper.toResponseList(List.of(first, second));

		assertEquals(2, responses.size());

		assertEquals("VAN-001", responses.get(0).id());
		assertEquals(420.0, responses.get(0).length());
		assertEquals(1000.0, responses.get(0).maxPayload());

		assertEquals("TRUCK-001", responses.get(1).id());
		assertEquals(620.0, responses.get(1).length());
		assertEquals(3500.0, responses.get(1).maxPayload());
	}
}