package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.entity.OutboxEventStatus;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class OutboxEventRelay {

	private static final Logger log =
			LoggerFactory.getLogger(OutboxEventRelay.class);

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final int batchSize;
	private final int maxAttempts;

	public OutboxEventRelay(
			final OutboxEventRepository outboxEventRepository,
			final KafkaTemplate<String, Object> kafkaTemplate,
			@Value("${app.kafka.outbox.batch-size:100}") final int batchSize,
			@Value("${app.kafka.outbox.max-attempts:5}") final int maxAttempts
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.kafkaTemplate = kafkaTemplate;
		this.batchSize = batchSize;
		this.maxAttempts = maxAttempts;
	}

	@Scheduled(
			fixedDelayString = "${app.kafka.outbox.relay-interval-ms:5000}"
	)
	@Transactional
	public void relayPendingEvents() {
		final List<OutboxEventEntity> batch =
				outboxEventRepository.findNextBatch(
						OutboxEventStatus.NEW,
						PageRequest.of(0, batchSize)
				);

		for (final OutboxEventEntity event : batch) {
			publish(event);
		}
	}

	private void publish(final OutboxEventEntity event) {
		try {
			kafkaTemplate
					.send(
							event.getTopicName(),
							event.getAggregateId(),
							event.getPayload()
					)
					.get();

			event.markPublished();

			log.info(
					"Outbox event published: eventId={}, topic={}",
					event.getEventId(),
					event.getTopicName()
			);
		} catch (final Exception exception) {
			if (event.getAttempts() + 1 >= maxAttempts) {
				event.markFailed();

				log.error(
						"Outbox event permanently failed after {} attempts: eventId={}, topic={}",
						event.getAttempts(),
						event.getEventId(),
						event.getTopicName(),
						exception
				);
			} else {
				event.increaseAttempts();

				log.warn(
						"Outbox event publish failed, will retry: eventId={}, topic={}, attempts={}",
						event.getEventId(),
						event.getTopicName(),
						event.getAttempts(),
						exception
				);
			}
		}
	}
}
