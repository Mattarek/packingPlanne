package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

/**
 * Template for outbox event publishers: writes a single row into the outbox
 * whenever there's something to publish. This is the write side of the
 * transactional outbox pattern; {@link OutboxEventRelay} is the read/publish
 * side, shared by every publisher.
 * <p>
 * Subclasses only supply the event-specific bits — how to build the wire
 * envelope, its type, target topic, and an aggregate id — the boilerplate
 * (skip-if-empty, serialize, persist) lives here once. To add a new
 * publisher: extend this class with your payload type, implement the four
 * abstract methods, and call {@link #publish(Object)} from wherever that
 * payload is produced (typically inside the same {@code @Transactional}
 * method that persists the underlying business change — see
 * {@link org.example.packing.application.service.PackageService#createPackages}).
 *
 * @param <T> the payload type this publisher turns into an outbox event
 */
public abstract class AbstractKafkaEventPublisher<T> {

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	protected AbstractKafkaEventPublisher(
			final OutboxEventRepository outboxEventRepository,
			final ObjectMapper objectMapper
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.objectMapper = objectMapper;
	}

	public final void publish(final T payload) {
		if (shouldSkip(payload)) {
			return;
		}

		final UUID eventId = UUID.randomUUID();
		final Instant occurredAt = Instant.now();
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
				UUID.randomUUID(),
				eventId,
				aggregateId(eventId, payload),
				eventType(),
				topicName(),
				serializedPayload,
				occurredAt
		));
	}

	/**
	 * Whether {@code payload} has nothing worth publishing — default: only
	 * {@code null}. Override for e.g. an empty collection.
	 */
	protected boolean shouldSkip(final T payload) {
		return payload == null;
	}

	/**
	 * Builds the object that gets JSON-serialized as the outbox row's
	 * payload — typically an envelope record carrying {@code eventId},
	 * {@code eventType}, a schema version, {@code occurredAt}, and the
	 * event-specific data derived from {@code payload}.
	 */
	protected abstract Object buildEnvelope(UUID eventId, Instant occurredAt, T payload);

	/**
	 * Identifies this event on the outbox row and doubles as the Kafka
	 * message key. Bounded by {@code outbox_events.aggregate_id}'s column
	 * size ({@code VARCHAR(100)}) — a value derived from {@code payload}
	 * that grows with its size (e.g. joining ids of every item in a batch)
	 * will eventually overflow it; {@code eventId} is a safe default for any
	 * publisher whose payload doesn't have one single natural business key.
	 */
	protected String aggregateId(final UUID eventId, final T payload) {
		return eventId.toString();
	}

	protected abstract String eventType();

	protected abstract String topicName();
}
