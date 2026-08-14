package org.example.packing.infrastructure.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestItem;
import org.example.packing.infrastructure.kafka.support.KafkaContainerHandler;
import org.example.packing.infrastructure.kafka.support.KafkaPostgresContainersInitializer;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.example.packing.infrastructure.persistence.repository.PackageRepository;
import org.example.packing.infrastructure.persistence.repository.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end proof for the one flow that had zero test coverage of any kind:
 * packages arriving through Kafka, then a packing run (real HTTP endpoint,
 * real Postgres, real Kafka) actually assigning them to vehicles.
 * <p>
 * {@code PackingRunnerService.runPacking()} loads <em>every</em> row from
 * {@code packages}/{@code vehicles} with no filtering, so this test wipes
 * both tables first — otherwise leftover rows from other test classes
 * sharing this same Postgres container (see {@code KafkaPostgresContainersInitializer})
 * would make the packing result unpredictable.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("kafka-integration-test")
@ContextConfiguration(initializers = KafkaPostgresContainersInitializer.class)
class PackingRunIntegrationTest {

	private static final String TOPIC = "package-create-requests";

	private static final int PACKAGE_COUNT = 40;

	// 20x20x20 cm cubes: 3 vehicles sized so no single one holds all 40,
	// but all 3 together comfortably do — forces (and proves) that packages
	// get spread across more than one vehicle.
	private static final double VEHICLE_CARGO_LENGTH = 100.0;
	private static final double VEHICLE_CARGO_WIDTH = 60.0;
	private static final double VEHICLE_CARGO_HEIGHT = 40.0;
	private static final double VEHICLE_MAX_PAYLOAD = 1_000.0;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PackageRepository packageRepository;

	@Autowired
	private VehicleRepository vehicleRepository;

	@BeforeEach
	void resetPackagesAndVehicles() {
		packageRepository.deleteAll();
		vehicleRepository.deleteAll();
	}

	@AfterEach
	void cleanUpPackagesAndVehicles() {
		packageRepository.deleteAll();
		vehicleRepository.deleteAll();
	}

	@Test
	void shouldAssignPackagesSentThroughKafkaToMultipleVehiclesOnPackingRun() throws Exception {
		// given: kilka pojazdów, z których żaden sam nie pomieści 40 paczek
		vehicleRepository.saveAll(List.of(
				vehicle("Van 1"),
				vehicle("Van 2"),
				vehicle("Van 3")
		));

		// when: 40 identycznych paczek wrzucone JEDNĄ wiadomością na Kafkę
		sendPackageCreateRequestEvent(PACKAGE_COUNT);

		await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
				assertThat(packageRepository.count()).isEqualTo(PACKAGE_COUNT));

		// and: uruchamiamy packing run przez prawdziwy endpoint HTTP
		final String responseJson = mockMvc.perform(post("/api/packing-runs"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		@SuppressWarnings("unchecked")
		final Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);

		// then: wszystkie 40 paczek zostały przydzielone, na więcej niż jeden pojazd
		assertThat(intValue(response, "totalPackagesIn")).isEqualTo(PACKAGE_COUNT);
		assertThat(intValue(response, "packedPackagesCount")).isEqualTo(PACKAGE_COUNT);
		assertThat((Boolean) response.get("fullyPacked")).isTrue();
		assertThat((List<?>) response.get("unpackedPackages")).isEmpty();
		assertThat(intValue(response, "vehiclesUsed")).isGreaterThanOrEqualTo(2);
	}

	private int intValue(final Map<String, Object> map, final String key) {
		return ((Number) map.get(key)).intValue();
	}

	private VehiclesEntity vehicle(final String name) {
		return new VehiclesEntity(
				null, name, VEHICLE_CARGO_LENGTH, VEHICLE_CARGO_WIDTH, VEHICLE_CARGO_HEIGHT, VEHICLE_MAX_PAYLOAD
		);
	}

	private void sendPackageCreateRequestEvent(final int packageCount) {
		final List<PackageCreateRequestItem> items = IntStream.range(0, packageCount)
				.mapToObj(i -> new PackageCreateRequestItem(
						20.0, 20.0, 20.0, 3.0, ProductCategory.STANDARD, FragilityLevel.STANDARD
				))
				.toList();

		final PackageCreateRequestEvent event = new PackageCreateRequestEvent(
				UUID.randomUUID(), "PACKAGE_CREATE_REQUESTED", 1, Instant.now(), items
		);

		send(objectMapper.writeValueAsString(event));
	}

	private void send(final String message) {
		final KafkaTemplate<String, String> kafkaTemplate = kafkaTemplate();
		kafkaTemplate.send(TOPIC, message);
		kafkaTemplate.flush();
	}

	private KafkaTemplate<String, String> kafkaTemplate() {
		final Map<String, Object> properties = Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				KafkaContainerHandler.container().getBootstrapServers(),

				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class,

				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class
		);

		return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(properties));
	}
}
