package org.example.packing.infrastructure.messaging;

import org.example.packing.infrastructure.messaging.outbox.OutboxEventRelay;
import org.example.packing.infrastructure.messaging.outbox.OutboxEventStatus;
import org.example.packing.infrastructure.messaging.outbox.OutboxProperties;
import org.example.packing.infrastructure.messaging.outbox.OutboxStore;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.example.packing.integration.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxEventRelayConcurrencyIT extends AbstractPostgresIntegrationTest {

	private static final int BATCH_SIZE = 3;
	private static final Duration SEND_DURATION = Duration.ofMillis(500);

	private final OutboxEventRepository outboxEventRepository;
	private final OutboxStore outboxStore;
	private final TransactionTemplate transactionTemplate;

	OutboxEventRelayConcurrencyIT(
			final OutboxEventRepository outboxEventRepository,
			final OutboxStore outboxStore,
			final TransactionTemplate transactionTemplate
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.outboxStore = outboxStore;
		this.transactionTemplate = transactionTemplate;
	}

	@BeforeEach
	void cleanTable() {
		outboxEventRepository.deleteAll();
	}

	@Test
	void twoInstancesShouldPublishDisjointBatchesInParallel() throws Exception {
		// given
		final Instant base = Instant.parse("2026-01-01T00:00:00Z");
		outboxEventRepository.saveAllAndFlush(IntStream.range(0, 2 * BATCH_SIZE)
				.mapToObj(i -> event("payload-" + i, base.plusSeconds(i)))
				.toList());

		final InFlightRecordingSender sender = new InFlightRecordingSender();
		final OutboxEventRelay instanceA = relay(sender);
		final OutboxEventRelay instanceB = relay(sender);
		final CountDownLatch start = new CountDownLatch(1);

		// when
		final CompletableFuture<Void> runA = runInTransaction(instanceA, start);
		final CompletableFuture<Void> runB = runInTransaction(instanceB, start);
		start.countDown();
		CompletableFuture.allOf(runA, runB).get(10, TimeUnit.SECONDS);

		// then
		assertThat(sender.sentPayloads())
				.hasSize(2 * BATCH_SIZE)
				.doesNotHaveDuplicates();
		assertThat(sender.maxInFlight()).isEqualTo(2 * BATCH_SIZE);
		assertThat(outboxEventRepository.findAll())
				.extracting(OutboxEventEntity::getStatus)
				.containsOnly(OutboxEventStatus.PUBLISHED);
	}

	private CompletableFuture<Void> runInTransaction(final OutboxEventRelay relay, final CountDownLatch start) {
		return CompletableFuture.runAsync(() -> {
			await(start);
			transactionTemplate.executeWithoutResult(status -> relay.relayPendingEvents());
		});
	}

	private OutboxEventRelay relay(final MessageSender sender) {
		return new OutboxEventRelay(
				outboxStore,
				sender,
				new OutboxProperties(BATCH_SIZE, 5, Duration.ofSeconds(5), Duration.ofDays(7)),
				Clock.systemUTC(),
				System::nanoTime
		);
	}

	private static OutboxEventEntity event(final String payload, final Instant createdAt) {
		final UUID eventId = UUID.randomUUID();
		return new OutboxEventEntity(
				UUID.randomUUID(), eventId, eventId.toString(), "TEST_EVENT", "topic", payload, createdAt
		);
	}

	private static void await(final CountDownLatch latch) {
		try {
			latch.await(10, TimeUnit.SECONDS);
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}

	private static final class InFlightRecordingSender implements MessageSender {

		private final Queue<String> sent = new ConcurrentLinkedQueue<>();
		private final AtomicInteger inFlight = new AtomicInteger();
		private final AtomicInteger maxInFlight = new AtomicInteger();

		@Override
		public CompletableFuture<Void> send(final String topic, final String key, final String payload) {
			sent.add(payload);
			maxInFlight.accumulateAndGet(inFlight.incrementAndGet(), Math::max);

			return CompletableFuture.runAsync(
					inFlight::decrementAndGet,
					CompletableFuture.delayedExecutor(SEND_DURATION.toMillis(), TimeUnit.MILLISECONDS)
			);
		}

		List<String> sentPayloads() {
			return List.copyOf(sent);
		}

		int maxInFlight() {
			return maxInFlight.get();
		}
	}
}
