package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.messaging.outbox.OutboxEventStatus;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.integration.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxEventRepositoryIT extends AbstractPostgresIntegrationTest {

	private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

	private final OutboxEventRepository outboxEventRepository;
	private final TransactionTemplate transactionTemplate;

	OutboxEventRepositoryIT(
			final OutboxEventRepository outboxEventRepository,
			final TransactionTemplate transactionTemplate
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.transactionTemplate = transactionTemplate;
	}

	@BeforeEach
	void cleanTable() {
		outboxEventRepository.deleteAll();
	}

	@Test
	void secondInstanceShouldSkipRowsLockedByTheFirstInsteadOfWaiting() throws Exception {
		// given
		final List<OutboxEventEntity> saved = outboxEventRepository.saveAllAndFlush(List.of(
				event(BASE), event(BASE.plusSeconds(1)), event(BASE.plusSeconds(2)), event(BASE.plusSeconds(3))
		));

		final CountDownLatch firstLocked = new CountDownLatch(1);
		final CountDownLatch releaseFirst = new CountDownLatch(1);

		final CompletableFuture<List<UUID>> instanceA = CompletableFuture.supplyAsync(() ->
				transactionTemplate.execute(status -> {
					final List<UUID> ids = idsOf(outboxEventRepository.findNextBatchSkippingLocked(
							OutboxEventStatus.NEW, PageRequest.of(0, 2)
					));
					firstLocked.countDown();
					await(releaseFirst);
					return ids;
				})
		);
		assertThat(firstLocked.await(10, TimeUnit.SECONDS)).isTrue();

		// when
		final List<UUID> instanceB = CompletableFuture.supplyAsync(() ->
				transactionTemplate.execute(status -> idsOf(outboxEventRepository.findNextBatchSkippingLocked(
						OutboxEventStatus.NEW, PageRequest.of(0, 10)
				)))
		).get(5, TimeUnit.SECONDS);

		releaseFirst.countDown();

		// then
		assertThat(instanceA.get(10, TimeUnit.SECONDS))
				.containsExactly(saved.get(0).getId(), saved.get(1).getId());
		assertThat(instanceB)
				.containsExactly(saved.get(2).getId(), saved.get(3).getId());
	}

	@Test
	void shouldDeleteOnlyPublishedEventsOlderThanTheCutoff() {
		// given
		final OutboxEventEntity oldPublished = event(BASE, OutboxEventStatus.PUBLISHED, BASE.plusSeconds(10));
		final OutboxEventEntity recentPublished = event(BASE, OutboxEventStatus.PUBLISHED, BASE.plusSeconds(1000));
		final OutboxEventEntity oldFailed = event(BASE, OutboxEventStatus.FAILED, null);
		final OutboxEventEntity pending = event(BASE);

		outboxEventRepository.saveAllAndFlush(List.of(oldPublished, recentPublished, oldFailed, pending));

		// when
		final int deleted = transactionTemplate.execute(status ->
				outboxEventRepository.deleteByStatusAndPublishedAtBefore(
						OutboxEventStatus.PUBLISHED, BASE.plusSeconds(500)
				)
		);

		// then
		assertThat(deleted).isEqualTo(1);
		assertThat(idsOf(outboxEventRepository.findAll()))
				.containsExactlyInAnyOrder(recentPublished.getId(), oldFailed.getId(), pending.getId());
	}

	private static OutboxEventEntity event(final Instant createdAt) {
		final UUID eventId = UUID.randomUUID();
		return new OutboxEventEntity(
				UUID.randomUUID(), eventId, eventId.toString(), "TEST_EVENT", "topic", "{}", createdAt
		);
	}

	private static OutboxEventEntity event(
			final Instant createdAt,
			final OutboxEventStatus status,
			final Instant publishedAt
	) {
		final UUID eventId = UUID.randomUUID();
		return new OutboxEventEntity(
				UUID.randomUUID(), eventId, eventId.toString(), "TEST_EVENT", "topic", "{}", createdAt,
				status, publishedAt, 1
		);
	}

	private static List<UUID> idsOf(final List<OutboxEventEntity> events) {
		return events.stream().map(OutboxEventEntity::getId).toList();
	}

	private static void await(final CountDownLatch latch) {
		try {
			latch.await(10, TimeUnit.SECONDS);
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}
}
