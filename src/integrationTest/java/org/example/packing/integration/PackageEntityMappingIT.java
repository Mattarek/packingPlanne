package org.example.packing.integration;

import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PackageEntityMappingIT extends AbstractPostgresIntegrationTest {
	private final PackageRepository packageRepository;
	private final JdbcTemplate jdbcTemplate;

	public PackageEntityMappingIT(final PackageRepository packageRepository, final JdbcTemplate jdbcTemplate) {
		this.packageRepository = packageRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test
	void shouldPersistPackageEntityToCorrectDatabaseColumns() {
		final PackageEntity packageEntity = new PackageEntity(
				null,
				10.0,
				20.0,
				30.0,
				5.5
		);

		packageRepository.saveAndFlush(packageEntity);

		final UUID generatedId = packageEntity.getId();

		assertNotNull(generatedId);

		final Map<String, Object> row = jdbcTemplate.queryForMap(
				"""
						SELECT id, length, width, height, weight
						FROM packages
						WHERE id = ?
						""",
				generatedId
		);

		assertEquals(generatedId, row.get("id"));
		assertEquals(10.0, ((Number) row.get("length")).doubleValue());
		assertEquals(20.0, ((Number) row.get("width")).doubleValue());
		assertEquals(30.0, ((Number) row.get("height")).doubleValue());
		assertEquals(5.5, ((Number) row.get("weight")).doubleValue());
	}
}
