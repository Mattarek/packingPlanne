package org.example.packing.infrastructure.messaging.outbox;

import org.example.packing.application.port.out.IdGenerator;
import org.example.packing.infrastructure.messaging.PayloadSerializationException;
import org.example.packing.infrastructure.messaging.PayloadSerializer;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public abstract class AbstractOutboxEventPublisher<T> {

	private final OutboxStore outboxStore;
	private final PayloadSerializer payloadSerializer;
	private final Clock clock;
	private final IdGenerator idGenerator;

	protected AbstractOutboxEventPublisher(
			final OutboxStore outboxStore,
			final PayloadSerializer payloadSerializer,
			final Clock clock,
			final IdGenerator idGenerator
	) {
		this.outboxStore = outboxStore;
		this.payloadSerializer = payloadSerializer;
		this.clock = clock;
		this.idGenerator = idGenerator;
	}

	public final void publish(final T payload) {
		if (shouldSkip(payload)) {
			return;
		}

		final UUID eventId = idGenerator.newId();
		final Instant occurredAt = clock.instant();
		final Object envelope = buildEnvelope(eventId, occurredAt, payload);

		final String serializedPayload;
		try {
			serializedPayload = payloadSerializer.serialize(envelope);
		} catch (final PayloadSerializationException exception) {
			throw new IllegalStateException(
					"Failed to serialize %s: eventId=%s".formatted(eventType(), eventId),
					exception
			);
		}

		outboxStore.add(OutboxMessage.pending(
				idGenerator.newId(),
				eventId,
				aggregateId(eventId, payload),
				eventType(),
				topicName(),
				serializedPayload,
				occurredAt
		));
	}

	protected boolean shouldSkip(final T payload) {
		return payload == null;
	}

	protected abstract Object buildEnvelope(UUID eventId, Instant occurredAt, T payload);

	protected String aggregateId(final UUID eventId, final T payload) {
		return eventId.toString();
	}

	protected abstract String eventType();

	protected abstract String topicName();
}
