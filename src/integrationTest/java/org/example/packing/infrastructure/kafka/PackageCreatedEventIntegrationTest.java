package org.example.packing.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.service.PackageService;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.kafka.support.KafkaContainerHandler;
import org.example.packing.infrastructure.kafka.support.KafkaPostgresContainersInitializer;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.entity.OutboxEventStatus;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end proof that the producer side is actually wired up: creating
 * packages through {@link PackageService} (the same entry point used by both
 * {@code PackageController} and {@code PackageCreateRequestProcessor}) writes
 * an outbox row, and the already-existing {@code OutboxEventRelay} — running
 * against a real Kafka broker here — picks it up and publishes it to the
 * {@code package-created-events} topic.
 */
@SpringBootTest
@ActiveProfiles("kafka-integration-test")
@ContextConfiguration(initializers = KafkaPostgresContainersInitializer.class)
class PackageCreatedEventIntegrationTest {

	private static final String TOPIC = "package-created-events";

	@Autowired
	private PackageService packageService;

	@Autowired
	private OutboxEventRepository outboxEventRepository;

	@Test
	void shouldPublishPackageCreatedEventAfterCreatingPackages() {
		// given
		final PackageRequest request = new PackageRequest(
				10.0, 20.0, 30.0, 5.5,
				ProductCategory.STANDARD, FragilityLevel.STANDARD
		);

		// when
		final List<PackageResponse> created = packageService.createPackages(List.of(request));
		final UUID packageId = created.get(0).id();

		// then: outbox row for this package eventually gets published
		// (filtered by payload content, not aggregateId — aggregateId is the
		// event id, since a batch can contain any number of packages and
		// joining all their ids would overflow outbox_events.aggregate_id)
		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			final List<OutboxEventEntity> events = outboxEventRepository.findAll().stream()
					.filter(event -> event.getPayload().contains(packageId.toString()))
					.toList();

			assertThat(events).hasSize(1);
			assertThat(events.get(0).getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
			assertThat(events.get(0).getAggregateId()).isEqualTo(events.get(0).getEventId().toString());
		});

		// and: the message actually landed on the real Kafka topic
		final List<ConsumerRecord<String, String>> records = collectRecords(
				TOPIC,
				1,
				Duration.ofSeconds(15),
				value -> value.contains(packageId.toString())
		);

		assertThat(records)
				.extracting(ConsumerRecord::value)
				.anySatisfy(value -> assertThat(value)
						.contains("PACKAGE_CREATED")
						.contains(packageId.toString()));
	}

	private List<ConsumerRecord<String, String>> collectRecords(
			final String topic,
			final int minCount,
			final Duration timeout,
			final Predicate<String> valueFilter
	) {
		final Map<String, Object> consumerProps = Map.of(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				KafkaContainerHandler.container().getBootstrapServers(),

				ConsumerConfig.GROUP_ID_CONFIG,
				"test-consumer-" + UUID.randomUUID(),

				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
				"earliest",

				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class,

				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class
		);

		final List<ConsumerRecord<String, String>> collected = new ArrayList<>();

		try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
			consumer.subscribe(List.of(topic));

			await()
					.atMost(timeout)
					.pollInSameThread()
					.untilAsserted(() -> {
						consumer.poll(Duration.ofMillis(200)).forEach(record -> {
							if (valueFilter.test(record.value())) {
								collected.add(record);
							}
						});
						assertThat(collected.size()).isGreaterThanOrEqualTo(minCount);
					});
		}

		return collected;
	}
}
