package org.example.packing.infrastructure.messaging.deadletter;

import org.example.packing.infrastructure.messaging.MessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadLetterRetrySchedulerTest {

	private static final Instant NOW = Instant.parse("2026-07-07T07:07:07Z");

	@Mock
	private DeadLetterStore store;

	@Mock
	private MessageSender messageSender;

	private DeadLetterRetryScheduler scheduler;

	@BeforeEach
	void setUp() {
		scheduler = new DeadLetterRetryScheduler(
				store,
				messageSender,
				new DeadLetterProperties(50, 5),
				Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	@Test
	void shouldResubmitPayloadToOriginalTopicAndSaveTheNewState() {
		// given
		final DeadLetter deadLetter = deadLetter();
		when(store.lockNextPendingRetrySkippingLocked(50)).thenReturn(List.of(deadLetter));
		when(messageSender.send("orig-topic", deadLetter.getEventId().toString(), "{\"a\":1}"))
				.thenReturn(CompletableFuture.completedFuture(null));

		// when
		scheduler.retryPendingDeadLetters();

		// then
		assertThat(deadLetter.getStatus()).isEqualTo(DeadLetterEventStatus.RESUBMITTED);
		assertThat(deadLetter.getLastRetriedAt()).isEqualTo(NOW);
		verify(store).save(deadLetter);
	}

	@Test
	void shouldLeaveDeadLetterPendingWhenResubmitFails() {
		// given
		final DeadLetter deadLetter = deadLetter();
		when(store.lockNextPendingRetrySkippingLocked(50)).thenReturn(List.of(deadLetter));
		when(messageSender.send(any(), any(), any()))
				.thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));

		// when
		scheduler.retryPendingDeadLetters();

		// then
		assertThat(deadLetter.getStatus()).isEqualTo(DeadLetterEventStatus.PENDING_RETRY);
		verify(store, never()).save(any());
	}

	private static DeadLetter deadLetter() {
		return DeadLetter.firstFailure(
				UUID.randomUUID(), UUID.randomUUID(), "ANY_EVENT", "orig-topic", "{\"a\":1}", "boom",
				NOW.minusSeconds(600)
		);
	}
}
