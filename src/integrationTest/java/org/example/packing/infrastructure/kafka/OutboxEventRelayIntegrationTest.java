package org.example.packing.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.packing.infrastructure.kafka.support.KafkaContainerHandler;
import org.example.packing.infrastructure.kafka.support.KafkaPostgresContainersInitializer;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.entity.OutboxEventStatus;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@ActiveProfiles("kafka-integration-test")
@ContextConfiguration(initializers = KafkaPostgresContainersInitializer.class)
class OutboxEventRelayIntegrationTest {

	private static final String TOPIC = "outbox-relay-test-topic";

	@Autowired
	private OutboxEventRepository outboxEventRepository;

	// Raw type on purpose: the auto-configured KafkaTemplate bean is declared
	// as KafkaTemplate<?, ?> in KafkaAutoConfiguration, and @MockitoSpyBean
	// matches beans by exact generic type — KafkaTemplate<String, String>
	// would not resolve to any bean and fail context startup.

	@Autowired
	@SuppressWarnings("rawtypes")
	private KafkaTemplate kafkaTemplate;

	@AfterEach
	void resetSpy() {
		Mockito.reset(kafkaTemplate);
	}

	@Test
	void shouldPublishNewOutboxEventAndMarkItPublished() {
		// given
		final String payload = "{\"hello\":\"world\"}";
		final OutboxEventEntity event = newOutboxEvent(UUID.randomUUID(), payload);
		outboxEventRepository.saveAndFlush(event);

		// when / then
		await().atMost(Duration.ofSeconds(15)).pollInterval(Duration.ofSeconds(1L)).untilAsserted(() -> {
			final OutboxEventEntity reloaded =
					outboxEventRepository.findById(event.getId()).orElseThrow();

			assertThat(reloaded.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
			assertThat(reloaded.getPublishedAt()).isNotNull();
		});

		final List<ConsumerRecord<String, String>> records =
				collectRecords(TOPIC, 1, Duration.ofSeconds(10), value -> value.equals(payload));

		assertThat(records)
				.extracting(ConsumerRecord::value)
				.contains(payload);
	}

	@Test
	void shouldEventuallyPublishAfterTransientKafkaFailures() {
		// given
		final AtomicInteger callCount = new AtomicInteger();

		doAnswer(invocation -> {
			if (callCount.getAndIncrement() < 2) {
				throw new RuntimeException("Simulated transient Kafka failure");
			}

			return invocation.callRealMethod();
		}).when(kafkaTemplate).send(anyString(), anyString(), anyString());

		final OutboxEventEntity event =
				newOutboxEvent(UUID.randomUUID(), "{\"retry\":\"me\"}");
		outboxEventRepository.saveAndFlush(event);

		// when / then
		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			final OutboxEventEntity reloaded =
					outboxEventRepository.findById(event.getId()).orElseThrow();

			assertThat(reloaded.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
			assertThat(reloaded.getAttempts()).isEqualTo(2);
		});
	}

	private OutboxEventEntity newOutboxEvent(
			final UUID eventId,
			final String payload
	) {
		return new OutboxEventEntity(
				UUID.randomUUID(),
				eventId,
				"aggregate-" + eventId,
				"TEST_EVENT",
				TOPIC,
				payload,
				Instant.now()
		);
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
