package org.example.packing.infrastructure.messaging.deadletter;

import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
import org.example.packing.infrastructure.messaging.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Component
@ConditionalOnKafkaEnabled
public class DeadLetterRetryScheduler {

	private static final Logger log =
			LoggerFactory.getLogger(DeadLetterRetryScheduler.class);

	private final DeadLetterStore deadLetterStore;
	private final MessageSender messageSender;
	private final int batchSize;
	private final Clock clock;

	public DeadLetterRetryScheduler(
			final DeadLetterStore deadLetterStore,
			final MessageSender messageSender,
			final DeadLetterProperties properties,
			final Clock clock
	) {
		this.deadLetterStore = deadLetterStore;
		this.messageSender = messageSender;
		this.batchSize = properties.batchSize();
		this.clock = clock;
	}

	@Scheduled(
			fixedDelayString = "${app.kafka.dead-letter.retry-interval-ms}"
	)
	@Transactional
	public void retryPendingDeadLetters() {
		final List<DeadLetter> batch = deadLetterStore.lockNextPendingRetrySkippingLocked(batchSize);

		for (final DeadLetter deadLetter : batch) {
			resubmit(deadLetter);
		}
	}

	private void resubmit(final DeadLetter deadLetter) {
		try {
			messageSender
					.send(
							deadLetter.getTopicName(),
							deadLetter.getEventId().toString(),
							deadLetter.getPayload()
					)
					.get();

			deadLetter.markResubmitted(clock.instant());
			deadLetterStore.save(deadLetter);

			log.info(
					"Dead-lettered event resubmitted: eventId={}, topic={}, attempts={}",
					deadLetter.getEventId(),
					deadLetter.getTopicName(),
					deadLetter.getAttempts()
			);
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
			logResubmitFailure(deadLetter, exception);
		} catch (final Exception exception) {
			logResubmitFailure(deadLetter, exception);
		}
	}

	private void logResubmitFailure(final DeadLetter deadLetter, final Exception exception) {
		log.warn(
				"Failed to resubmit dead-lettered event, will retry next cycle: eventId={}, topic={}",
				deadLetter.getEventId(),
				deadLetter.getTopicName(),
				exception
		);
	}
}
