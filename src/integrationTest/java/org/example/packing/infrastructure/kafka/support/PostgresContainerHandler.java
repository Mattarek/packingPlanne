package org.example.packing.infrastructure.kafka.support;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.postgresql.PostgreSQLContainer;

public final class PostgresContainerHandler extends ContainerSetupHandler {

	private static final PostgreSQLContainer POSTGRES =
			new PostgreSQLContainer("postgres:17")
					.withDatabaseName("app_db")
					.withUsername("app_user")
					.withPassword("app_password");

	static {
		POSTGRES.start();
	}

	public static PostgreSQLContainer container() {
		return POSTGRES;
	}

	@Override
	protected void setUp(final ConfigurableApplicationContext context) {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
				context,
				"spring.datasource.url=" + POSTGRES.getJdbcUrl(),
				"spring.datasource.username=" + POSTGRES.getUsername(),
				"spring.datasource.password=" + POSTGRES.getPassword()
		);
	}
}
