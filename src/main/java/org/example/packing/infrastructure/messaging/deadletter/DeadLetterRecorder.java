package org.example.packing.infrastructure.messaging.deadletter;

import org.example.packing.application.port.out.IdGenerator;
import org.example.packing.infrastructure.messaging.PayloadSerializationException;
import org.example.packing.infrastructure.messaging.PayloadSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class DeadLetterRecorder {

	private static final Logger log = LoggerFactory.getLogger(DeadLetterRecorder.class);

	private final DeadLetterStore deadLetterStore;
	private final PayloadSerializer payloadSerializer;
	private final Clock clock;
	private final IdGenerator idGenerator;
	private final int maxRetryAttempts;

	public DeadLetterRecorder(
			final DeadLetterStore deadLetterStore,
			final PayloadSerializer payloadSerializer,
			final Clock clock,
			final IdGenerator idGenerator,
			final DeadLetterProperties properties
	) {
		this.deadLetterStore = deadLetterStore;
		this.payloadSerializer = payloadSerializer;
		this.clock = clock;
		this.idGenerator = idGenerator;
		this.maxRetryAttempts = properties.maxAttempts();
	}

	@Transactional
	public void record(
			final UUID eventId,
			final String eventType,
			final String topicName,
			final Object event,
			final String reason
	) {
		if (eventId == null || event == null) {
			return;
		}

		final Instant now = clock.instant();

		deadLetterStore.findByEventId(eventId)
				.ifPresentOrElse(
						existing -> {
							existing.recordFailure(reason, maxRetryAttempts, now);
							deadLetterStore.save(existing);
						},
						() -> recordFirstFailure(eventId, eventType, topicName, event, reason, now)
				);
	}

	private void recordFirstFailure(
			final UUID eventId,
			final String eventType,
			final String topicName,
			final Object event,
			final String reason,
			final Instant now
	) {
		final String payload;
		try {
			payload = payloadSerializer.serialize(event);
		} catch (final PayloadSerializationException exception) {
			log.warn(
					"Could not serialize dead-lettered event for storage: eventId={}",
					eventId,
					exception
			);

			return;
		}

		deadLetterStore.save(DeadLetter.firstFailure(
				idGenerator.newId(),
				eventId,
				eventType,
				topicName,
				payload,
				reason,
				now
		));
	}
}
