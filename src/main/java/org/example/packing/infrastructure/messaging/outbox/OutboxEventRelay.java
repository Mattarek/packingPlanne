package org.example.packing.infrastructure.messaging.outbox;

import org.example.packing.application.port.out.MonotonicClock;
import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
import org.example.packing.infrastructure.messaging.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnKafkaEnabled
public class OutboxEventRelay {

	private static final Logger log =
			LoggerFactory.getLogger(OutboxEventRelay.class);

	private final OutboxStore outboxStore;
	private final MessageSender messageSender;
	private final int batchSize;
	private final int maxAttempts;
	private final Duration sendTimeout;
	private final Clock clock;
	private final MonotonicClock monotonicClock;

	public OutboxEventRelay(
			final OutboxStore outboxStore,
			final MessageSender messageSender,
			final OutboxProperties properties,
			final Clock clock,
			final MonotonicClock monotonicClock
	) {
		this.outboxStore = outboxStore;
		this.messageSender = messageSender;
		this.batchSize = properties.batchSize();
		this.maxAttempts = properties.maxAttempts();
		this.sendTimeout = properties.sendTimeout();
		this.clock = clock;
		this.monotonicClock = monotonicClock;
	}

	@Scheduled(
			fixedDelayString = "${app.kafka.outbox.relay-interval-ms}"
	)
	@Transactional
	public void relayPendingEvents() {
		final List<OutboxMessage> batch = outboxStore.lockNextPendingSkippingLocked(batchSize);

		final List<CompletableFuture<?>> sends = batch.stream()
				.map(this::send)
				.toList();

		final long deadline = monotonicClock.nanoTime() + sendTimeout.toNanos();

		for (int i = 0; i < batch.size(); i++) {
			awaitResult(batch.get(i), sends.get(i), deadline);
		}

		outboxStore.saveAll(batch);
	}

	private CompletableFuture<?> send(final OutboxMessage event) {
		try {
			return messageSender.send(event.getTopicName(), event.getAggregateId(), event.getPayload());
		} catch (final RuntimeException exception) {
			return CompletableFuture.failedFuture(exception);
		}
	}

	private void awaitResult(
			final OutboxMessage event,
			final CompletableFuture<?> send,
			final long deadline
	) {
		try {
			final long remainingNanos = Math.max(0, deadline - monotonicClock.nanoTime());
			send.get(remainingNanos, TimeUnit.NANOSECONDS);

			event.markPublished(clock.instant());

			log.info(
					"Outbox event published: eventId={}, topic={}",
					event.getEventId(),
					event.getTopicName()
			);
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
			recordFailure(event, exception);
		} catch (final Exception exception) {
			recordFailure(event, exception);
		}
	}

	private void recordFailure(final OutboxMessage event, final Exception exception) {
		event.recordFailedAttempt(maxAttempts);

		if (event.isFailed()) {
			log.error(
					"Outbox event permanently failed after {} attempts: eventId={}, topic={}",
					event.getAttempts(),
					event.getEventId(),
					event.getTopicName(),
					exception
			);
		} else {
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
