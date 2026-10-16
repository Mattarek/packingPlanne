package org.example.packing.infrastructure.kafka.deadletter;

import org.example.packing.application.port.IdGenerator;
import org.example.packing.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.example.packing.infrastructure.persistence.repository.DeadLetterEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class DeadLetterRecorder {

	private static final Logger log = LoggerFactory.getLogger(DeadLetterRecorder.class);

	private final DeadLetterEventRepository deadLetterEventRepository;
	private final ObjectMapper objectMapper;
	private final Clock clock;
	private final IdGenerator idGenerator;
	private final int maxRetryAttempts;

	public DeadLetterRecorder(
			final DeadLetterEventRepository deadLetterEventRepository,
			final ObjectMapper objectMapper,
			final Clock clock,
			final IdGenerator idGenerator,
			@Value("${app.kafka.dead-letter.max-attempts:5}") final int maxRetryAttempts
	) {
		this.deadLetterEventRepository = deadLetterEventRepository;
		this.objectMapper = objectMapper;
		this.clock = clock;
		this.idGenerator = idGenerator;
		this.maxRetryAttempts = maxRetryAttempts;
	}

	/**
	 * @param eventId null when the record was a poison pill (deserialization
	 * 		failed, so there is nothing to key a row on) – then only logged
	 */
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

		deadLetterEventRepository.findByEventId(eventId)
				.ifPresentOrElse(
						existing -> existing.recordFailure(reason, maxRetryAttempts, now),
						() -> createRow(eventId, eventType, topicName, event, reason, now)
				);
	}

	private void createRow(
			final UUID eventId,
			final String eventType,
			final String topicName,
			final Object event,
			final String reason,
			final Instant now
	) {
		final String payload;
		try {
			payload = objectMapper.writeValueAsString(event);
		} catch (final JacksonException exception) {
			// It was deserialized from JSON moments ago, so this is not
			// expected to happen; if it somehow does, don't lose the
			// original failure to a secondary serialization error.
			log.warn(
					"Could not serialize dead-lettered event for storage: eventId={}",
					eventId,
					exception
			);

			return;
		}

		deadLetterEventRepository.save(new DeadLetterEventEntity(
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
