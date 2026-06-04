package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.domain.model.Package;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackagePersistenceMapperTest {

	private final PackagePersistenceMapper mapper =
			Mappers.getMapper(PackagePersistenceMapper.class);

	@Test
	void shouldMapRequestToEntity() {
		final PackageRequest request = new PackageRequest(
				"PKG-001",
				90.0,
				60.0,
				50.0,
				200.0
		);

		final PackageEntity entity = mapper.toEntity(request);

		assertEquals("PKG-001", entity.getId());
		assertEquals(90.0, entity.getLength());
		assertEquals(60.0, entity.getWidth());
		assertEquals(50.0, entity.getHeight());
		assertEquals(200.0, entity.getWeight());
	}

	@Test
	void shouldMapEntityToResponse() {
		final PackageEntity entity = new PackageEntity(
				"PKG-001",
				90.0,
				60.0,
				50.0,
				200.0
		);

		final PackageResponse response = mapper.toResponse(entity);

		assertEquals("PKG-001", response.id());
		assertEquals(90.0, response.length());
		assertEquals(60.0, response.width());
		assertEquals(50.0, response.height());
		assertEquals(200.0, response.weight());
	}

	@Test
	void shouldMapEntityToDomain() {
		final PackageEntity entity = new PackageEntity(
				"PKG-001",
				90.0,
				60.0,
				50.0,
				200.0
		);

		final Package domain = mapper.toDomain(entity);

		assertEquals("PKG-001", domain.id());
		assertEquals(90.0, domain.dimensions().length());
		assertEquals(60.0, domain.dimensions().width());
		assertEquals(50.0, domain.dimensions().height());
		assertEquals(200.0, domain.weight().kilograms());
	}

	@Test
	void shouldMapEntityListToResponseList() {
		final PackageEntity first = new PackageEntity("PKG-001", 90.0, 60.0, 50.0, 200.0);
		final PackageEntity second = new PackageEntity("PKG-002", 40.0, 20.0, 10.0, 10.0);

		final List<PackageResponse> responses = mapper.toResponseList(List.of(first, second));

		assertEquals(2, responses.size());

		assertEquals("PKG-001", responses.get(0).id());
		assertEquals(90.0, responses.get(0).length());
		assertEquals(200.0, responses.get(0).weight());

		assertEquals("PKG-002", responses.get(1).id());
		assertEquals(40.0, responses.get(1).length());
		assertEquals(10.0, responses.get(1).weight());
	}
}