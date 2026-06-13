package org.example.packing.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("integration-test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class DatabaseSchemaIT extends AbstractPostgresIntegrationTest {

	private final String selectPackagesTableCount = """
			SELECT COUNT(*)
			FROM information_schema.tables
			WHERE table_name = 'packages'
			""";

	private final String selectPackagesColumns = """
			SELECT column_name
			FROM information_schema.columns
			WHERE table_name = 'packages'
			""";

	private final String insertPackage = """
			INSERT INTO packages(id, length, width, height, weight)
			VALUES (?, ?, ?, ?, ?)
			""";

	private final String selectPackageCountById = """
			SELECT COUNT(*)
			FROM packages
			WHERE id = ?
			""";

	private final String selectPackageById = """
			SELECT id, length, width, height, weight
			FROM packages
			WHERE id = ?
			""";

	private final String selectPackageIdById = """
			SELECT id
			FROM packages
			WHERE id = ?
			""";

	private final String deletePackageById = """
			DELETE FROM packages
			WHERE id = ?
			""";

	private final String updatePackageDimensionsAndWeight = """
			UPDATE packages
			SET length = ?, width = ?, height = ?, weight = ?
			WHERE id = ?
			""";

	private final String selectPackageDimensionsAndWeightById = """
			SELECT length, width, height, weight
			FROM packages
			WHERE id = ?
			""";

	private final JdbcTemplate jdbcTemplate;

	DatabaseSchemaIT(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test
	void shouldHavePackagesTable() {
		final Integer count = jdbcTemplate.queryForObject(
				selectPackagesTableCount,
				Integer.class
		);

		assertEquals(1, count);
	}

	@Test
	void shouldHaveRequiredColumnsInPackagesTable() {
		final List<String> columns = jdbcTemplate.queryForList(
				selectPackagesColumns,
				String.class
		);

		assertTrue(columns.contains("id"));
		assertTrue(columns.contains("length"));
		assertTrue(columns.contains("width"));
		assertTrue(columns.contains("height"));
		assertTrue(columns.contains("weight"));
	}

	@Test
	void shouldInsertPackageIntoDatabase() {
		final UUID id = UUID.randomUUID();

		insertPackage(id, 10.0, 20.0, 30.0, 5.5);

		final Integer count = jdbcTemplate.queryForObject(
				selectPackageCountById,
				Integer.class,
				id
		);

		assertEquals(1, count);
	}

	@Test
	void shouldReadInsertedPackageFromDatabase() {
		final UUID id = UUID.randomUUID();

		insertPackage(id, 10.0, 20.0, 30.0, 5.5);

		final Map<String, Object> row = jdbcTemplate.queryForMap(
				selectPackageById,
				id
		);

		assertEquals(id, row.get("id"));
		assertEquals(10.0, ((Number) row.get("length")).doubleValue());
		assertEquals(20.0, ((Number) row.get("width")).doubleValue());
		assertEquals(30.0, ((Number) row.get("height")).doubleValue());
		assertEquals(5.5, ((Number) row.get("weight")).doubleValue());
	}

	@Test
	void shouldStoreProvidedUuidAsPackageId() {
		final UUID id = UUID.randomUUID();

		insertPackage(id, 10.0, 20.0, 30.0, 5.5);

		final UUID savedId = jdbcTemplate.queryForObject(
				selectPackageIdById,
				UUID.class,
				id
		);

		assertEquals(id, savedId);
	}

	@Test
	void shouldNotAllowNullId() {
		assertThrows(DataIntegrityViolationException.class, () -> {
			jdbcTemplate.update(
					insertPackage,
					null,
					10.0,
					20.0,
					30.0,
					5.5
			);
		});
	}

	@Test
	void shouldNotAllowNullLength() {
		final UUID id = UUID.randomUUID();

		assertThrows(DataIntegrityViolationException.class, () -> {
			jdbcTemplate.update(
					insertPackage,
					id,
					null,
					20.0,
					30.0,
					5.5
			);
		});
	}

	@Test
	void shouldNotAllowNullWidth() {
		final UUID id = UUID.randomUUID();

		assertThrows(DataIntegrityViolationException.class, () -> {
			jdbcTemplate.update(
					insertPackage,
					id,
					10.0,
					null,
					30.0,
					5.5
			);
		});
	}

	@Test
	void shouldNotAllowNullHeight() {
		final UUID id = UUID.randomUUID();

		assertThrows(DataIntegrityViolationException.class, () -> {
			jdbcTemplate.update(
					insertPackage,
					id,
					10.0,
					20.0,
					null,
					5.5
			);
		});
	}

	@Test
	void shouldNotAllowNullWeight() {
		final UUID id = UUID.randomUUID();

		assertThrows(DataIntegrityViolationException.class, () -> {
			jdbcTemplate.update(
					insertPackage,
					id,
					10.0,
					20.0,
					30.0,
					null
			);
		});
	}

	@Test
	void shouldDeletePackageFromDatabase() {
		final UUID id = UUID.randomUUID();

		insertPackage(id, 10.0, 20.0, 30.0, 5.5);

		jdbcTemplate.update(
				deletePackageById,
				id
		);

		final Integer count = jdbcTemplate.queryForObject(
				selectPackageCountById,
				Integer.class,
				id
		);

		assertEquals(0, count);
	}

	@Test
	void shouldUpdatePackageDimensionsAndWeight() {
		final UUID id = UUID.randomUUID();

		insertPackage(id, 10.0, 20.0, 30.0, 5.5);

		jdbcTemplate.update(
				updatePackageDimensionsAndWeight,
				15.0,
				25.0,
				35.0,
				6.5,
				id
		);

		final Map<String, Object> row = jdbcTemplate.queryForMap(
				selectPackageDimensionsAndWeightById,
				id
		);

		assertEquals(15.0, ((Number) row.get("length")).doubleValue());
		assertEquals(25.0, ((Number) row.get("width")).doubleValue());
		assertEquals(35.0, ((Number) row.get("height")).doubleValue());
		assertEquals(6.5, ((Number) row.get("weight")).doubleValue());
	}

	private void insertPackage(
			final UUID id,
			final double length,
			final double width,
			final double height,
			final double weight
	) {
		jdbcTemplate.update(
				insertPackage,
				id,
				length,
				width,
				height,
				weight
		);
	}
}