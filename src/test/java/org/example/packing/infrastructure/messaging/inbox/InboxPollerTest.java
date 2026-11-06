package org.example.packing.infrastructure.messaging.inbox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboxPollerTest {

	private static final Instant NOW = Instant.parse("2026-04-04T04:04:04Z");
	private static final UUID CLAIM_TOKEN = UUID.fromString("00000000-0000-0000-0000-0000000000cc");
	private static final int BATCH_SIZE = 3;
	private static final Duration LEASE = Duration.ofMinutes(5);

	@Mock
	private InboxQueue inboxQueue;

	@Mock
	private InboxMessageExecutor messageExecutor;

	private InboxPoller poller;

	@BeforeEach
	void setUp() {
		poller = new InboxPoller(
				inboxQueue,
				messageExecutor,
				() -> CLAIM_TOKEN,
				Clock.fixed(NOW, ZoneOffset.UTC),
				new InboxProperties(
						BATCH_SIZE, 2, LEASE, 5,
						Duration.ofSeconds(1), Duration.ofMinutes(1), Duration.ofDays(7), 100
				)
		);
	}

	@AfterEach
	void tearDown() throws InterruptedException {
		poller.shutdown();
	}

	@Test
	void shouldClaimWithLeaseAndDoNothingWhenQueueIsEmpty() {
		// given
		when(inboxQueue.claimAvailable(BATCH_SIZE, NOW, NOW.plus(LEASE), CLAIM_TOKEN)).thenReturn(List.of());

		// when
		poller.processAvailableMessages();

		// then
		verifyNoInteractions(messageExecutor);
	}

	@Test
	void shouldKeepDrainingWhileBatchesAreFull() {
		// given
		final List<InboxMessage> full = messages(BATCH_SIZE);
		final List<InboxMessage> partial = messages(1);
		when(inboxQueue.claimAvailable(anyInt(), any(), any(), any())).thenReturn(full).thenReturn(partial);

		// when
		poller.processAvailableMessages();

		// then
		verify(inboxQueue, times(2)).claimAvailable(eq(BATCH_SIZE), eq(NOW), eq(NOW.plus(LEASE)), eq(CLAIM_TOKEN));
		full.forEach(message -> verify(messageExecutor).execute(message));
		partial.forEach(message -> verify(messageExecutor).execute(message));
	}

	@Test
	void shouldProcessRestOfBatchWhenOneMessageBlowsUp() {
		// given
		final List<InboxMessage> batch = messages(2);
		when(inboxQueue.claimAvailable(anyInt(), any(), any(), any())).thenReturn(batch);
		doThrow(new IllegalStateException("db down")).when(messageExecutor).execute(batch.getFirst());

		// when
		poller.processAvailableMessages();

		// then
		verify(messageExecutor).execute(batch.get(1));
	}

	private static List<InboxMessage> messages(final int count) {
		return IntStream.range(0, count)
				.mapToObj(i -> new InboxMessage(
						UUID.randomUUID(), UUID.randomUUID(), "TYPE", 1, "topic", "{}", NOW, 0, CLAIM_TOKEN
				))
				.toList();
	}
}
