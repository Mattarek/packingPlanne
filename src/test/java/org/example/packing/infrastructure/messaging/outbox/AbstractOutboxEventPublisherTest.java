package org.example.packing.infrastructure.messaging.outbox;

import org.example.packing.application.port.out.IdGenerator;
import org.example.packing.infrastructure.messaging.PayloadSerializationException;
import org.example.packing.infrastructure.messaging.PayloadSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class AbstractOutboxEventPublisherTest {

	private static final PayloadSerializer SERIALIZER = payload -> "serialized:" + payload;

	@Mock
	private OutboxStore outboxStore;

	private RecordingPublisher publisher;

	@BeforeEach
	void setUp() {
		publisher = new RecordingPublisher(outboxStore, SERIALIZER, Clock.systemUTC(), UUID::randomUUID);
	}

	@Test
	void shouldSkipNullPayloadByDefault() {
		// when
		publisher.publish(null);

		// then
		verifyNoInteractions(outboxStore);
	}

	@Test
	void shouldAddPendingMessageBuiltFromEnvelope() {
		// when
		publisher.publish("some-payload");

		// then
		final OutboxMessage added = captureAdded();
		assertThat(added.getTopicName()).isEqualTo("recording-topic");
		assertThat(added.getEventType()).isEqualTo("RECORDING_EVENT");
		assertThat(added.getPayload()).startsWith("serialized:").contains("some-payload");
		assertThat(added.getStatus()).isEqualTo(OutboxEventStatus.NEW);
	}

	@Test
	void shouldUseInjectedClockAndIdGenerator() {
		// given
		final Instant now = Instant.parse("2026-03-01T10:15:30Z");
		final UUID eventId = UUID.fromString("00000000-0000-0000-0000-00000000000a");
		final UUID messageId = UUID.fromString("00000000-0000-0000-0000-00000000000b");
		final Iterator<UUID> ids = List.of(eventId, messageId).iterator();
		final RecordingPublisher deterministic = new RecordingPublisher(
				outboxStore, SERIALIZER, Clock.fixed(now, ZoneOffset.UTC), ids::next
		);

		// when
		deterministic.publish("some-payload");

		// then
		final OutboxMessage added = captureAdded();
		assertThat(added.getEventId()).isEqualTo(eventId);
		assertThat(added.getId()).isEqualTo(messageId);
		assertThat(added.getCreatedAt()).isEqualTo(now);
	}

	@Test
	void shouldDefaultAggregateIdToTheGeneratedEventId() {
		// when
		publisher.publish("some-payload");

		// then
		final OutboxMessage added = captureAdded();
		assertThat(added.getAggregateId()).isEqualTo(added.getEventId().toString());
	}

	@Test
	void shouldAllowOverridingAggregateId() {
		// given
		final RecordingPublisher withCustomAggregateId = new RecordingPublisher(
				outboxStore, SERIALIZER, Clock.systemUTC(), UUID::randomUUID
		) {
			@Override
			protected String aggregateId(final UUID eventId, final String payload) {
				return "aggregate-" + payload;
			}
		};

		// when
		withCustomAggregateId.publish("some-payload");

		// then
		assertThat(captureAdded().getAggregateId()).isEqualTo("aggregate-some-payload");
	}

	@Test
	void shouldWrapSerializationFailureInIllegalStateException() {
		// given
		final RecordingPublisher failingPublisher = new RecordingPublisher(
				outboxStore,
				payload -> {
					throw new PayloadSerializationException("boom", new RuntimeException());
				},
				Clock.systemUTC(),
				UUID::randomUUID
		);

		// when & then
		assertThatThrownBy(() -> failingPublisher.publish("some-payload"))
				.isInstanceOf(IllegalStateException.class);

		verifyNoInteractions(outboxStore);
	}

	private OutboxMessage captureAdded() {
		final ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
		verify(outboxStore).add(captor.capture());
		return captor.getValue();
	}

	private static class RecordingPublisher extends AbstractOutboxEventPublisher<String> {

		RecordingPublisher(
				final OutboxStore outboxStore,
				final PayloadSerializer payloadSerializer,
				final Clock clock,
				final IdGenerator idGenerator
		) {
			super(outboxStore, payloadSerializer, clock, idGenerator);
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
