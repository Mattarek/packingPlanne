package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.application.service.PackageCreateRequestProcessor;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.example.packing.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.example.packing.infrastructure.persistence.repository.DeadLetterEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class PackageCreateRequestConsumer extends AbstractKafkaConsumer<PackageCreateRequestEvent> {

	private static final Logger log = LoggerFactory.getLogger(PackageCreateRequestConsumer.class);

	private static final String EVENT_TYPE = "PACKAGE_CREATE_REQUESTED";

	private final PackageCreateRequestProcessor processor;
	private final DeadLetterEventRepository deadLetterEventRepository;
	private final ObjectMapper objectMapper;
	private final String topicName;
	private final int maxRetryAttempts;

	public PackageCreateRequestConsumer(
			final PackageCreateRequestProcessor processor,
			final DeadLetterEventRepository deadLetterEventRepository,
			final ObjectMapper objectMapper,
			@Value("${app.kafka.topics.package-create-requests}") final String topicName,
			@Value("${app.kafka.dead-letter.max-attempts:5}") final int maxRetryAttempts
	) {
		this.processor = processor;
		this.deadLetterEventRepository = deadLetterEventRepository;
		this.objectMapper = objectMapper;
		this.topicName = topicName;
		this.maxRetryAttempts = maxRetryAttempts;
	}

	@RetryableTopic(
			attempts = "${app.kafka.retry.attempts:3}",
			backOff = @BackOff(delayString = "${app.kafka.retry.backoff-ms:5000}"),
			exclude = NonRetryableKafkaProcessingException.class,
			dltTopicSuffix = ".DLT",
			numPartitions = "${app.kafka.topics.partitions:3}",
			autoCreateTopics = "true"
	)
	@KafkaListener(
			topics = "${app.kafka.topics.package-create-requests}", // czyta z topicu package-create-requests
			groupId = "${spring.kafka.consumer.group-id}" // nalezy do consumer groupy packing-group
	)
	public void consume(final PackageCreateRequestEvent event) {
		handle(event);
	}

	@Override
	protected void process(final PackageCreateRequestEvent event) {
		processor.process(event);
	}

	// Metoda jest uzywana, @DltHandler jest uzywany przez kafke automatycznie przez Springa,
	// przez mechanizm listenerow
	@DltHandler
	@Transactional
	public void handleDlt(
			final PackageCreateRequestEvent event,
			@Header(KafkaHeaders.EXCEPTION_MESSAGE) final String exceptionMessage
	) {
		// event is null when the record itself is a poison pill (e.g. malformed
		// JSON): deserialization fails again on the DLT topic itself, there is
		// no eventId to key a dead-letter row on, so this stays log-only.
		log.error(
				"Package create event sent to DLT: eventId={}, reason={}",
				event == null ? null : event.eventId(),
				exceptionMessage
		);

		recordDeadLetter(event, exceptionMessage);
	}

	private void recordDeadLetter(
			final PackageCreateRequestEvent event,
			final String reason
	) {
		if (event == null || event.eventId() == null) {
			return;
		}

		final Instant now = Instant.now();

		deadLetterEventRepository.findByEventId(event.eventId())
				.ifPresentOrElse(
						existing -> existing.recordFailure(reason, maxRetryAttempts, now),
						() -> createDeadLetterRow(event, reason, now)
				);
	}

	private void createDeadLetterRow(
			final PackageCreateRequestEvent event,
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
					event.eventId(),
					exception
			);

			return;
		}

		deadLetterEventRepository.save(new DeadLetterEventEntity(
				UUID.randomUUID(),
				event.eventId(),
				EVENT_TYPE,
				topicName,
				payload,
				reason,
				now
		));
	}
}