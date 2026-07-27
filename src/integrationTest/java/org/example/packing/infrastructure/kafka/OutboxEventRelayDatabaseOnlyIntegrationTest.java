package org.example.packing.infrastructure.kafka;

import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.entity.OutboxEventStatus;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

/**
 * Wariant testujący samą warstwę bazodanową relaya: OutboxEventRelay
 * korzysta tu z w pełni zamockowanego {@link KafkaTemplate} (żadnej
 * prawdziwej publikacji), więc do testu potrzebny jest wyłącznie
 * kontener Postgresa — bez Kafki.
 */
@SpringBootTest
@ActiveProfiles("kafka-integration-test")
class OutboxEventRelayDatabaseOnlyIntegrationTest extends AbstractPostgresIntegrationTest {

	private static final String TOPIC = "outbox-relay-test-topic";

	@Autowired
	private OutboxEventRepository outboxEventRepository;

	@MockitoBean
	@SuppressWarnings("rawtypes")
	private KafkaTemplate kafkaTemplate;

	@Test
	void shouldMarkOutboxEventFailedAfterExhaustingMaxAttempts() {
		// given
		doThrow(new RuntimeException("Simulated persistent Kafka failure"))
				.when(kafkaTemplate).send(anyString(), anyString(), anyString());

		final OutboxEventEntity event =
				newOutboxEvent(UUID.randomUUID(), "{\"never\":\"published\"}");
		outboxEventRepository.saveAndFlush(event);

		// when / then
		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			final OutboxEventEntity reloaded =
					outboxEventRepository.findById(event.getId()).orElseThrow();

			assertThat(reloaded.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
			assertThat(reloaded.getAttempts()).isEqualTo(5);
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
}
