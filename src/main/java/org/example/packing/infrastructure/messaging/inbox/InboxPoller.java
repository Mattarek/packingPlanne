package org.example.packing.infrastructure.messaging.inbox;

import jakarta.annotation.PreDestroy;
import org.example.packing.application.port.out.IdGenerator;
import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnKafkaEnabled
public class InboxPoller {

	private static final Logger log = LoggerFactory.getLogger(InboxPoller.class);

	private final InboxQueue inboxQueue;
	private final InboxMessageExecutor messageExecutor;
	private final IdGenerator idGenerator;
	private final Clock clock;
	private final int batchSize;
	private final Duration lease;
	private final ExecutorService workers;

	public InboxPoller(
			final InboxQueue inboxQueue,
			final InboxMessageExecutor messageExecutor,
			final IdGenerator idGenerator,
			final Clock clock,
			final InboxProperties properties
	) {
		this.inboxQueue = inboxQueue;
		this.messageExecutor = messageExecutor;
		this.idGenerator = idGenerator;
		this.clock = clock;
		this.batchSize = properties.batchSize();
		this.lease = properties.lease();
		this.workers = Executors.newFixedThreadPool(
				properties.workerThreads(),
				Thread.ofPlatform().name("inbox-worker-", 0).factory()
		);
	}

	@Scheduled(fixedDelayString = "${app.kafka.inbox.poll-interval-ms}")
	public void processAvailableMessages() {
		List<InboxMessage> batch;
		do {
			batch = claimNextBatch();
			processConcurrently(batch);
		} while (batch.size() == batchSize && !Thread.currentThread().isInterrupted());
	}

	@PreDestroy
	void shutdown() throws InterruptedException {
		workers.shutdown();
		if (!workers.awaitTermination(lease.toMillis(), TimeUnit.MILLISECONDS)) {
			workers.shutdownNow();
		}
	}

	private List<InboxMessage> claimNextBatch() {
		final Instant now = clock.instant();
		return inboxQueue.claimAvailable(batchSize, now, now.plus(lease), idGenerator.newId());
	}

	private void processConcurrently(final List<InboxMessage> batch) {
		if (batch.isEmpty()) {
			return;
		}

		CompletableFuture.allOf(batch.stream()
						.map(message -> CompletableFuture.runAsync(() -> executeSafely(message), workers))
						.toArray(CompletableFuture[]::new))
				.join();
	}

	private void executeSafely(final InboxMessage message) {
		try {
			messageExecutor.execute(message);
		} catch (final RuntimeException exception) {
			log.error(
					"Inbox message bookkeeping failed, lease expiry will release it: eventId={}",
					message.eventId(),
					exception
			);
		}
	}
}
