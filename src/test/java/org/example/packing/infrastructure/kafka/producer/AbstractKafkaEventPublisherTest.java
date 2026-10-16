package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.application.port.IdGenerator;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AbstractKafkaEventPublisherTest {

	@Mock
	private OutboxEventRepository outboxEventRepository;

	private RecordingPublisher publisher;

	@BeforeEach
	void setUp() {
		publisher = new RecordingPublisher(outboxEventRepository, JsonMapper.builder().build());
	}

	@Test
	void shouldSkipNullPayloadByDefault() {
		// when
		publisher.publish(null);

		// then
		verifyNoInteractions(outboxEventRepository);
	}

	@Test
	void shouldSaveOutboxEventBuiltFromEnvelope() {
		// when
		publisher.publish("some-payload");

		// then
		final ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
		verify(outboxEventRepository).save(captor.capture());

		final OutboxEventEntity saved = captor.getValue();
		assertThat(saved.getTopicName()).isEqualTo("recording-topic");
		assertThat(saved.getEventType()).isEqualTo("RECORDING_EVENT");
		assertThat(saved.getPayload()).contains("some-payload");
	}

	@Test
	void shouldUseInjectedClockAndIdGenerator() {
		// given: D4 – time and ids come from abstractions, so they are assertable
		final Instant now = Instant.parse("2026-03-01T10:15:30Z");
		final UUID eventId = UUID.fromString("00000000-0000-0000-0000-00000000000a");
		final UUID rowId = UUID.fromString("00000000-0000-0000-0000-00000000000b");
		final Iterator<UUID> ids = List.of(eventId, rowId).iterator();

		final RecordingPublisher deterministic = new RecordingPublisher(
				outboxEventRepository,
				JsonMapper.builder().build(),
				Clock.fixed(now, ZoneOffset.UTC),
				ids::next
		);

		// when
		deterministic.publish("some-payload");

		// then
		final ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
		verify(outboxEventRepository).save(captor.capture());

		final OutboxEventEntity saved = captor.getValue();
		assertThat(saved.getEventId()).isEqualTo(eventId);
		assertThat(saved.getId()).isEqualTo(rowId);
		assertThat(saved.getCreatedAt()).isEqualTo(now);
	}

	@Test
	void shouldDefaultAggregateIdToTheGeneratedEventId() {
		// when
		publisher.publish("some-payload");

		// then
		final ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
		verify(outboxEventRepository).save(captor.capture());

		final OutboxEventEntity saved = captor.getValue();
		assertThat(saved.getAggregateId()).isEqualTo(saved.getEventId().toString());
	}

	@Test
	void shouldAllowOverridingAggregateId() {
		// given
		final RecordingPublisher withCustomAggregateId = new RecordingPublisher(
				outboxEventRepository,
				JsonMapper.builder().build()
		) {
			@Override
			protected String aggregateId(final UUID eventId, final String payload) {
				return "aggregate-" + payload;
			}
		};

		// when
		withCustomAggregateId.publish("some-payload");

		// then
		final ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
		verify(outboxEventRepository).save(captor.capture());

		assertThat(captor.getValue().getAggregateId()).isEqualTo("aggregate-some-payload");
	}

	@Test
	void shouldWrapSerializationFailureInIllegalStateException() {
		// given: buildEnvelope returns an object whose getter always throws,
		// which Jackson surfaces as a JacksonException while serializing.
		final RecordingPublisher failingPublisher = new RecordingPublisher(
				outboxEventRepository,
				JsonMapper.builder().build()
		) {
			@Override
			protected Object buildEnvelope(final UUID eventId, final Instant occurredAt, final String payload) {
				return new UnserializableEnvelope();
			}
		};

		// when & then
		assertThatThrownBy(() -> failingPublisher.publish("some-payload"))
				.isInstanceOf(IllegalStateException.class);

		verifyNoInteractions(outboxEventRepository);
	}

	public static final class UnserializableEnvelope {

		public String getValue() {
			throw new RuntimeException("boom");
		}
	}

	private static class RecordingPublisher extends AbstractKafkaEventPublisher<String> {

		RecordingPublisher(final OutboxEventRepository outboxEventRepository, final ObjectMapper objectMapper) {
			this(outboxEventRepository, objectMapper, Clock.systemUTC(), UUID::randomUUID);
		}

		RecordingPublisher(
				final OutboxEventRepository outboxEventRepository,
				final ObjectMapper objectMapper,
				final Clock clock,
				final IdGenerator idGenerator
		) {
			super(outboxEventRepository, objectMapper, clock, idGenerator);
		}

		@Override
		protected Object buildEnvelope(final UUID eventId, final Instant occurredAt, final String payload) {
			return new RecordedEnvelope(eventId, occurredAt, payload);
		}

		@Override
		protected String eventType() {
			return "RECORDING_EVENT";
		}

		@Override
		protected String topicName() {
			return "recording-topic";
		}
	}

	private record RecordedEnvelope(UUID eventId, Instant occurredAt, String payload) {
	}
}
