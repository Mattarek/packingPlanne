package org.example.packing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
class PackingApplicationTests {

	private static final String POSTGRES_IMAGE = "postgres:17";
	private static final String DATABASE_NAME = "packing_test";
	private static final String USERNAME = "test";
	private static final String PASSWORD = "test";

	@ServiceConnection
	private static final PostgreSQLContainer<?> POSTGRES =
			new PostgreSQLContainer<>(POSTGRES_IMAGE)
					.withDatabaseName(DATABASE_NAME)
					.withUsername(USERNAME)
					.withPassword(PASSWORD);

	static {
		POSTGRES.start();
	}

	@Test
	void contextLoads() {
	}
}
