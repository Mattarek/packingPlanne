package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.example.packing.infrastructure.persistence.entity.DeadLetterEventStatus;
import org.example.packing.infrastructure.persistence.repository.DeadLetterEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Gives dead-lettered events (see {@code PackageCreateRequestConsumer#handleDlt})
 * a chance to succeed on their own, without a human having to notice and
 * manually replay them: on a schedule, resubmit whatever is still
 * {@link DeadLetterEventStatus#PENDING_RETRY} back onto its original topic,
 * running it through the normal inbox + processing pipeline again.
 * <p>
 * Mirrors {@link OutboxEventRelay}'s batch/interval/attempts shape, just in
 * the opposite direction (inbound recovery instead of outbound publishing).
 */
@Component
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class DeadLetterRetryScheduler {

	private static final Logger log =
			LoggerFactory.getLogger(DeadLetterRetryScheduler.class);

	private final DeadLetterEventRepository deadLetterEventRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final int batchSize;

	public DeadLetterRetryScheduler(
			final DeadLetterEventRepository deadLetterEventRepository,
			final KafkaTemplate<String, Object> kafkaTemplate,
			@Value("${app.kafka.dead-letter.batch-size:50}") final int batchSize
	) {
		this.deadLetterEventRepository = deadLetterEventRepository;
		this.kafkaTemplate = kafkaTemplate;
		this.batchSize = batchSize;
	}

	@Scheduled(
			fixedDelayString = "${app.kafka.dead-letter.retry-interval-ms:60000}"
	)
	@Transactional
	public void retryPendingDeadLetters() {
		final List<DeadLetterEventEntity> batch =
				deadLetterEventRepository.findNextBatch(
						DeadLetterEventStatus.PENDING_RETRY,
						PageRequest.of(0, batchSize)
				);

		for (final DeadLetterEventEntity event : batch) {
			resubmit(event);
		}
	}

	private void resubmit(final DeadLetterEventEntity event) {
		try {
			kafkaTemplate
					.send(
							event.getTopicName(),
							event.getEventId().toString(),
							event.getPayload()
					)
					.get();

			event.markResubmitted(Instant.now());

			log.info(
					"Dead-lettered event resubmitted: eventId={}, topic={}, attempts={}",
					event.getEventId(),
					event.getTopicName(),
					event.getAttempts()
			);
		} catch (final Exception exception) {
			log.warn(
					"Failed to resubmit dead-lettered event, will retry next cycle: eventId={}, topic={}",
					event.getEventId(),
					event.getTopicName(),
					exception
			);
		}
	}
}
