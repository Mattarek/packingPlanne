package org.example.packing.infrastructure.messaging.inbox;

import org.example.packing.infrastructure.messaging.deadletter.DeadLetterRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboxMessageExecutorTest {

	private static final Instant NOW = Instant.parse("2026-04-04T04:04:04Z");
	private static final int MAX_ATTEMPTS = 3;

	@Mock
	private InboxQueue inboxQueue;

	@Mock
	private InboxLedger inboxLedger;

	@Mock
	private InboxMessageDispatcher dispatcher;

	@Mock
	private DeadLetterRecorder deadLetterRecorder;

	@Mock
	private PlatformTransactionManager transactionManager;

	private InboxMessageExecutor executor;

	@BeforeEach
	void setUp() {
		executor = new InboxMessageExecutor(
				inboxQueue,
				inboxLedger,
				dispatcher,
				deadLetterRecorder,
				transactionManager,
				new InboxProperties(
						10, 1, Duration.ofMinutes(5), MAX_ATTEMPTS,
						Duration.ofSeconds(1), Duration.ofMinutes(1), Duration.ofDays(7), 100
				),
				Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	@Test
	void shouldDeleteClaimedMessageBeforeDispatchingIt() {
		// given
		final InboxMessage message = message(0);
		when(inboxQueue.deleteClaimed(message.id(), message.claimToken())).thenReturn(true);

		// when
		executor.execute(message);

		// then
		final InOrder order = inOrder(inboxQueue, dispatcher);
		order.verify(inboxQueue).deleteClaimed(message.id(), message.claimToken());
		order.verify(dispatcher).dispatch(message);
		verifyNoInteractions(deadLetterRecorder, inboxLedger);
	}

	@Test
	void shouldSkipMessageWhoseLeaseWasTakenOver() {
		// given
		final InboxMessage message = message(0);
		when(inboxQueue.deleteClaimed(message.id(), message.claimToken())).thenReturn(false);

		// when
		executor.execute(message);

		// then
		verifyNoInteractions(dispatcher, deadLetterRecorder);
	}

	@Test
	void shouldReleaseMessageWithBackoffAfterTransientFailure() {
		// given
		final InboxMessage message = message(1);
		when(inboxQueue.deleteClaimed(message.id(), message.claimToken())).thenReturn(true);
		doThrow(new IllegalStateException("db hiccup")).when(dispatcher).dispatch(message);

		// when
		executor.execute(message);

		// then
		verify(inboxQueue).releaseForRetry(
				eq(message.id()),
				eq(message.claimToken()),
				eq(NOW.plusSeconds(2)),
				startsWith("IllegalStateException: db hiccup")
		);
		verifyNoInteractions(deadLetterRecorder);
	}

	@Test
	void shouldDeadLetterAfterExhaustingAttempts() {
		// given
		final InboxMessage message = message(MAX_ATTEMPTS - 1);
		when(inboxQueue.deleteClaimed(message.id(), message.claimToken())).thenReturn(true);
		doThrow(new IllegalStateException("still failing")).when(dispatcher).dispatch(message);

		// when
		executor.execute(message);

		// then
		verify(inboxQueue, never()).releaseForRetry(any(), any(), any(), anyString());
		verify(inboxLedger).forget(message.eventId());
		verify(deadLetterRecorder).recordSerialized(
				eq(message.eventId()),
				eq("TYPE"),
				eq("topic"),
				eq("{}"),
				startsWith("IllegalStateException")
		);
	}

	@Test
	void shouldDeadLetterPermanentFailureImmediately() {
		// given
		final InboxMessage message = message(0);
		when(inboxQueue.deleteClaimed(message.id(), message.claimToken())).thenReturn(true);
		doThrow(new PermanentInboxFailureException("bad payload")).when(dispatcher).dispatch(message);

		// when
		executor.execute(message);

		// then
		verify(inboxQueue, never()).releaseForRetry(any(), any(), any(), anyString());
		verify(inboxLedger).forget(message.eventId());
		verify(deadLetterRecorder).recordSerialized(
				eq(message.eventId()),
				eq("TYPE"),
				eq("topic"),
				eq("{}"),
				eq("PermanentInboxFailureException: bad payload")
		);
	}

	private static InboxMessage message(final int attempts) {
		return new InboxMessage(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"TYPE",
				1,
				"topic",
				"{}",
				NOW.minusSeconds(60),
				attempts,
				UUID.randomUUID()
		);
	}
}
