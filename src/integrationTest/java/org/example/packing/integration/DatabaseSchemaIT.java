package org.example.packing.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class DatabaseSchemaIT extends AbstractPostgresIntegrationTest {

	private final JdbcTemplate jdbcTemplate;

	DatabaseSchemaIT(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test
	void shouldCreatePackagesTableUsingLiquibase() {
		// when
		final Integer tableCount = jdbcTemplate.queryForObject(
				"""
						select count(*)
						from information_schema.tables
						where table_schema = 'public'
						  and table_name = 'packages'
						""",
				Integer.class
		);

		// then
		assertThat(tableCount).isEqualTo(1);
	}

	@Test
	void shouldCreateVehiclesTableUsingLiquibase() {
		// when
		final Integer tableCount = jdbcTemplate.queryForObject(
				"""
						select count(*)
						from information_schema.tables
						where table_schema = 'public'
						  and table_name = 'vehicles'
						""",
				Integer.class
		);

		// then
		assertThat(tableCount).isEqualTo(1);
	}

	@Test
	void shouldCreateLiquibaseChangelogTable() {
		// when
		final Integer tableCount = jdbcTemplate.queryForObject(
				"""
						select count(*)
						from information_schema.tables
						where table_schema = 'public'
						  and table_name = 'databasechangelog'
						""",
				Integer.class
		);

		// then
		assertThat(tableCount).isEqualTo(1);
	}
}