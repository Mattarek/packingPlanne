package org.example.packing.infrastructure.messaging.inbox;

import org.example.packing.infrastructure.messaging.deadletter.DeadLetterRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;

@Component
public class InboxMessageExecutor {

	private static final Logger log = LoggerFactory.getLogger(InboxMessageExecutor.class);

	private static final int MAX_ERROR_LENGTH = 2000;

	private final InboxQueue inboxQueue;
	private final InboxLedger inboxLedger;
	private final InboxMessageDispatcher dispatcher;
	private final DeadLetterRecorder deadLetterRecorder;
	private final TransactionTemplate transactionTemplate;
	private final ExponentialBackoff backoff;
	private final int maxAttempts;
	private final Clock clock;

	public InboxMessageExecutor(
			final InboxQueue inboxQueue,
			final InboxLedger inboxLedger,
			final InboxMessageDispatcher dispatcher,
			final DeadLetterRecorder deadLetterRecorder,
			final PlatformTransactionManager transactionManager,
			final InboxProperties properties,
			final Clock clock
	) {
		this.inboxQueue = inboxQueue;
		this.inboxLedger = inboxLedger;
		this.dispatcher = dispatcher;
		this.deadLetterRecorder = deadLetterRecorder;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.backoff = new ExponentialBackoff(properties.initialBackoff(), properties.maxBackoff());
		this.maxAttempts = properties.maxAttempts();
		this.clock = clock;
	}

	public void execute(final InboxMessage message) {
		try {
			transactionTemplate.executeWithoutResult(status -> processAndDelete(message));
		} catch (final PermanentInboxFailureException exception) {
			deadLetter(message, exception);
		} catch (final RuntimeException exception) {
			retryOrDeadLetter(message, exception);
		}
	}

	private void processAndDelete(final InboxMessage message) {
		if (!inboxQueue.deleteClaimed(message.id(), message.claimToken())) {
			log.warn("Inbox message lease lost, skipping: eventId={}", message.eventId());
			return;
		}

		dispatcher.dispatch(message);

		log.debug("Inbox message processed: eventId={}", message.eventId());
	}

	private void retryOrDeadLetter(final InboxMessage message, final RuntimeException exception) {
		final int failedAttempts = message.attemptsAfterFailure();

		if (failedAttempts >= maxAttempts) {
			deadLetter(message, exception);
			return;
		}

		final Instant availableAt = clock.instant().plus(backoff.delayAfter(failedAttempts));

		transactionTemplate.executeWithoutResult(status ->
				inboxQueue.releaseForRetry(message.id(), message.claimToken(), availableAt, describe(exception))
		);

		log.warn(
				"Inbox message failed, will retry: eventId={}, attempts={}, availableAt={}",
				message.eventId(),
				failedAttempts,
				availableAt,
				exception
		);
	}

	private void deadLetter(final InboxMessage message, final RuntimeException exception) {
		transactionTemplate.executeWithoutResult(status -> {
			if (!inboxQueue.deleteClaimed(message.id(), message.claimToken())) {
				return;
			}

			inboxLedger.forget(message.eventId());
			deadLetterRecorder.recordSerialized(
					message.eventId(),
					message.eventType(),
					message.sourceTopic(),
					message.payload(),
					describe(exception)
			);
		});

		log.error(
				"Inbox message dead-lettered: eventId={}, attempts={}",
				message.eventId(),
				message.attemptsAfterFailure(),
				exception
		);
	}

	private static String describe(final RuntimeException exception) {
		final String description = exception.getClass().getSimpleName() + ": " + exception.getMessage();
		return description.length() <= MAX_ERROR_LENGTH
				? description
				: description.substring(0, MAX_ERROR_LENGTH);
	}
}
