package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.messaging.inbox.IncomingMessage;
import org.example.packing.infrastructure.messaging.inbox.InboxLedger;
import org.example.packing.infrastructure.messaging.inbox.InboxMessage;
import org.example.packing.infrastructure.messaging.inbox.InboxQueue;
import org.example.packing.infrastructure.persistence.entity.InboxMessageEntity;
import org.example.packing.integration.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InboxRepositoriesIT extends AbstractPostgresIntegrationTest {

	private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant LEASE_UNTIL = BASE.plusSeconds(3600);

	private final InboxQueue inboxQueue;
	private final InboxLedger inboxLedger;
	private final InboxMessageRepository inboxMessageRepository;
	private final InboxEventRepository inboxEventRepository;
	private final TransactionTemplate transactionTemplate;

	InboxRepositoriesIT(
			final InboxQueue inboxQueue,
			final InboxLedger inboxLedger,
			final InboxMessageRepository inboxMessageRepository,
			final InboxEventRepository inboxEventRepository,
			final TransactionTemplate transactionTemplate
	) {
		this.inboxQueue = inboxQueue;
		this.inboxLedger = inboxLedger;
		this.inboxMessageRepository = inboxMessageRepository;
		this.inboxEventRepository = inboxEventRepository;
		this.transactionTemplate = transactionTemplate;
	}

	@BeforeEach
	void cleanTables() {
		inboxMessageRepository.deleteAllInBatch();
		inboxEventRepository.deleteAllInBatch();
	}

	@Test
	void shouldIgnoreSecondEnqueueOfTheSameEvent() {
		// given
		final UUID eventId = UUID.randomUUID();

		// when
		inboxQueue.enqueue(message(eventId, BASE));
		inboxQueue.enqueue(message(eventId, BASE.plusSeconds(1)));

		// then
		assertThat(inboxMessageRepository.count()).isEqualTo(1);
	}

	@Test
	void shouldClaimOnlyAvailableMessagesOldestFirstAndStampTheLease() {
		// given
		final InboxMessage oldest = enqueue(BASE);
		final InboxMessage newer = enqueue(BASE.plusSeconds(1));
		enqueue(BASE.plusSeconds(100));
		final UUID token = UUID.randomUUID();

		// when
		final List<InboxMessage> claimed = inboxQueue.claimAvailable(10, BASE.plusSeconds(10), LEASE_UNTIL, token);

		// then
		assertThat(claimed).extracting(InboxMessage::id).containsExactly(oldest.id(), newer.id());
		assertThat(claimed).allSatisfy(message -> assertThat(message.claimToken()).isEqualTo(token));
		assertThat(inboxMessageRepository.findById(oldest.id()))
				.get()
				.extracting(InboxMessageEntity::getAvailableAt)
				.isEqualTo(LEASE_UNTIL);
	}

	@Test
	void shouldNotClaimMessageWhoseLeaseHasNotExpired() {
		// given
		enqueue(BASE);
		inboxQueue.claimAvailable(10, BASE, LEASE_UNTIL, UUID.randomUUID());

		// when
		final List<InboxMessage> secondClaim =
				inboxQueue.claimAvailable(10, LEASE_UNTIL.minusSeconds(1), LEASE_UNTIL.plusSeconds(60), UUID.randomUUID());

		// then
		assertThat(secondClaim).isEmpty();
	}

	@Test
	void shouldReclaimMessageAfterLeaseExpiryAndRejectTheStaleOwner() {
		// given
		enqueue(BASE);
		final InboxMessage staleClaim = inboxQueue.claimAvailable(10, BASE, LEASE_UNTIL, UUID.randomUUID()).getFirst();
		final InboxMessage freshClaim =
				inboxQueue.claimAvailable(10, LEASE_UNTIL, LEASE_UNTIL.plusSeconds(60), UUID.randomUUID()).getFirst();

		// when
		final boolean staleDeleted = inboxQueue.deleteClaimed(staleClaim.id(), staleClaim.claimToken());
		final boolean freshDeleted = inboxQueue.deleteClaimed(freshClaim.id(), freshClaim.claimToken());

		// then
		assertThat(staleDeleted).isFalse();
		assertThat(freshDeleted).isTrue();
		assertThat(inboxMessageRepository.count()).isZero();
	}

	@Test
	void secondWorkerShouldSkipRowsLockedByTheFirstInsteadOfWaiting() throws Exception {
		// given
		final List<InboxMessage> enqueued = IntStream.range(0, 4)
				.mapToObj(i -> enqueue(BASE.plusSeconds(i)))
				.toList();
		final CountDownLatch firstClaimed = new CountDownLatch(1);
		final CountDownLatch releaseFirst = new CountDownLatch(1);

		final CompletableFuture<List<UUID>> workerA = CompletableFuture.supplyAsync(() ->
				transactionTemplate.execute(status -> {
					final List<UUID> ids = idsOf(inboxQueue.claimAvailable(2, BASE.plusSeconds(10), LEASE_UNTIL, UUID.randomUUID()));
					firstClaimed.countDown();
					await(releaseFirst);
					return ids;
				})
		);
		assertThat(firstClaimed.await(10, TimeUnit.SECONDS)).isTrue();

		// when
		final List<UUID> workerB = CompletableFuture.supplyAsync(() ->
				idsOf(inboxQueue.claimAvailable(10, BASE.plusSeconds(10), LEASE_UNTIL, UUID.randomUUID()))
		).get(5, TimeUnit.SECONDS);

		releaseFirst.countDown();

		// then
		assertThat(workerA.get(10, TimeUnit.SECONDS))
				.containsExactly(enqueued.get(0).id(), enqueued.get(1).id());
		assertThat(workerB)
				.containsExactly(enqueued.get(2).id(), enqueued.get(3).id());
	}

	@Test
	void shouldReleaseForRetryWithIncrementedAttemptsAndError() {
		// given
		enqueue(BASE);
		final InboxMessage claimed = inboxQueue.claimAvailable(10, BASE, LEASE_UNTIL, UUID.randomUUID()).getFirst();
		final Instant retryAt = BASE.plusSeconds(30);

		// when
		final boolean released = inboxQueue.releaseForRetry(claimed.id(), claimed.claimToken(), retryAt, "boom");

		// then
		assertThat(released).isTrue();
		final InboxMessageEntity row = inboxMessageRepository.findById(claimed.id()).orElseThrow();
		assertThat(row.getAttempts()).isEqualTo(1);
		assertThat(row.getAvailableAt()).isEqualTo(retryAt);
		assertThat(row.getClaimToken()).isNull();
		assertThat(row.getLastError()).isEqualTo("boom");
		assertThat(inboxQueue.claimAvailable(10, retryAt.minusSeconds(1), LEASE_UNTIL, UUID.randomUUID())).isEmpty();
		assertThat(inboxQueue.claimAvailable(10, retryAt, LEASE_UNTIL, UUID.randomUUID()))
				.singleElement()
				.extracting(InboxMessage::attempts)
				.isEqualTo(1);
	}

	@Test
	void shouldRegisterLedgerEntryOnceAndAllowItAgainAfterForget() {
		// given
		final UUID eventId = UUID.randomUUID();

		// when
		final boolean first = inboxLedger.registerIfAbsent(UUID.randomUUID(), eventId, "TYPE", BASE);
		final boolean duplicate = inboxLedger.registerIfAbsent(UUID.randomUUID(), eventId, "TYPE", BASE);
		inboxLedger.forget(eventId);
		final boolean afterForget = inboxLedger.registerIfAbsent(UUID.randomUUID(), eventId, "TYPE", BASE);

		// then
		assertThat(first).isTrue();
		assertThat(duplicate).isFalse();
		assertThat(afterForget).isTrue();
	}

	@Test
	void shouldDeleteExpiredLedgerEntriesInBoundedChunks() {
		// given
		IntStream.range(0, 5).forEach(i ->
				inboxLedger.registerIfAbsent(UUID.randomUUID(), UUID.randomUUID(), "TYPE", BASE.plusSeconds(i))
		);
		inboxLedger.registerIfAbsent(UUID.randomUUID(), UUID.randomUUID(), "TYPE", BASE.plusSeconds(1000));

		// when
		final int firstChunk = inboxLedger.deleteReceivedBefore(BASE.plusSeconds(500), 3);
		final int secondChunk = inboxLedger.deleteReceivedBefore(BASE.plusSeconds(500), 3);
		final int thirdChunk = inboxLedger.deleteReceivedBefore(BASE.plusSeconds(500), 3);

		// then
		assertThat(firstChunk).isEqualTo(3);
		assertThat(secondChunk).isEqualTo(2);
		assertThat(thirdChunk).isZero();
		assertThat(inboxEventRepository.count()).isEqualTo(1);
	}

	private InboxMessage enqueue(final Instant receivedAt) {
		final InboxMessage message = message(UUID.randomUUID(), receivedAt);
		inboxQueue.enqueue(message);
		return message;
	}

	private static InboxMessage message(final UUID eventId, final Instant receivedAt) {
		return InboxMessage.received(
				UUID.randomUUID(),
				new IncomingMessage(eventId, "TYPE", 1, "topic", "{\"payload\":true}"),
				receivedAt
		);
	}

	private static List<UUID> idsOf(final List<InboxMessage> messages) {
		return messages.stream().map(InboxMessage::id).toList();
	}

	private static void await(final CountDownLatch latch) {
		try {
			latch.await(10, TimeUnit.SECONDS);
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}
}
