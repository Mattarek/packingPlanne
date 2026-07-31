package org.example.packing.infrastructure.kafka;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
abstract class AbstractKafkaPostgresIntegrationTest {
	@Container
	static final KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");
	@Container
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	@DynamicPropertySource
	static void registerProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
	}
}
