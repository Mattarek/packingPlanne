package org.example.packing.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("integration-test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class AbstractPostgresIntegrationTest {

	private static final String POSTGRES_IMAGE = "postgres:16-alpine";
	private static final String DATABASE_NAME = "packing_test";
	private static final String USERNAME = "test";
	private static final String PASSWORD = "test";

	@ServiceConnection
	protected static final PostgreSQLContainer<?> POSTGRES =
			new PostgreSQLContainer<>(POSTGRES_IMAGE)
					.withDatabaseName(DATABASE_NAME)
					.withUsername(USERNAME)
					.withPassword(PASSWORD);

	static {
		POSTGRES.start();
	}
}