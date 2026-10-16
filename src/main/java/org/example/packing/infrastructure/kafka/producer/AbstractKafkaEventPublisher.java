package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.application.port.IdGenerator;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public abstract class AbstractKafkaEventPublisher<T> {

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;
	private final Clock clock;
	private final IdGenerator idGenerator;

	protected AbstractKafkaEventPublisher(
			final OutboxEventRepository outboxEventRepository,
			final ObjectMapper objectMapper,
			final Clock clock,
			final IdGenerator idGenerator
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.objectMapper = objectMapper;
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
			serializedPayload = objectMapper.writeValueAsString(envelope);
		} catch (final JacksonException exception) {
			throw new IllegalStateException(
					"Failed to serialize %s: eventId=%s".formatted(eventType(), eventId),
					exception
			);
		}

		outboxEventRepository.save(new OutboxEventEntity(
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
