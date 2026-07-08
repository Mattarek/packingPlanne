package org.example.packing.infrastructure.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.packing.application.service.PackageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest
@ActiveProfiles("kafka-integration-test")
class PackageCreateRequestConsumerIntegrationTest {

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
			.withDatabaseName("app_db")
			.withUsername("app_user")
			.withPassword("app_password");
	@Container
	static final KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");
	private static final String TOPIC = "package-create-requests";
	@Autowired
	private PackageService packageService;

	@DynamicPropertySource
	static void registerProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
		registry.add("spring.kafka.consumer.group-id", () -> "packing-group-test");
		registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");

		registry.add(
				"spring.kafka.consumer.key-deserializer",
				() -> "org.apache.kafka.common.serialization.StringDeserializer"
		);
		registry.add(
				"spring.kafka.consumer.value-deserializer",
				() -> "org.apache.kafka.common.serialization.StringDeserializer"
		);

		registry.add("app.kafka.enabled", () -> "true");
		registry.add("app.kafka.topics.package-create-requests", () -> TOPIC);
	}

	@Test
	void shouldCreatePackagesFromKafkaMessage() {
		// given
		final KafkaTemplate<String, String> kafkaTemplate = kafkaTemplate();

		final String message = """
				[
				  {
				    "length": 10.0,
				    "width": 20.0,
				    "height": 30.0,
				    "weight": 5.5
				  },
				  {
				    "length": 15.0,
				    "width": 25.0,
				    "height": 35.0,
				    "weight": 7.2
				  }
				]
				""";

		// when
		kafkaTemplate.send(TOPIC, message);
		kafkaTemplate.flush();

		// then
		await()
				.untilAsserted(() -> assertThat(
						packageService.getPackages(0, 20).getTotalElements()
				).isEqualTo(2));
	}

	private KafkaTemplate<String, String> kafkaTemplate() {
		final Map<String, Object> properties = Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				kafka.getBootstrapServers(),

				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class,

				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class
		);

		return new KafkaTemplate<>(
				new DefaultKafkaProducerFactory<>(properties)
		);
	}
}