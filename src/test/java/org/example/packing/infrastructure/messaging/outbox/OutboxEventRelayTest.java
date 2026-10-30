package org.example.packing.infrastructure.messaging.outbox;

import org.example.packing.infrastructure.messaging.MessageSender;
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
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayTest {

	private static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");
	private static final Duration SEND_TIMEOUT = Duration.ofMillis(300);

	@Mock
	private OutboxStore outboxStore;

	@Mock
	private MessageSender messageSender;

	private OutboxEventRelay relay;

	@BeforeEach
	void setUp() {
		relay = new OutboxEventRelay(
				outboxStore,
				messageSender,
				new OutboxProperties(100, 5, SEND_TIMEOUT, Duration.ofDays(7)),
				Clock.fixed(NOW, ZoneOffset.UTC),
				System::nanoTime
		);
	}

	@Test
	void shouldMarkAcknowledgedMessagesPublishedAndSaveTheBatch() {
		// given
		final OutboxMessage message = message("a");
		givenBatch(message);
		when(messageSender.send(anyString(), anyString(), eq("a"))).thenReturn(acked());

		// when
		relay.relayPendingEvents();

		// then
		assertThat(message.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
		assertThat(message.getPublishedAt()).isEqualTo(NOW);
		verify(outboxStore).saveAll(List.of(message));
	}

	@Test
	void shouldNotLetOneHangingSendBlockTheRestOfTheBatch() {
		// given
		final OutboxMessage hanging = message("hanging");
		final OutboxMessage fine = message("fine");
		givenBatch(hanging, fine);
		when(messageSender.send(anyString(), anyString(), eq("hanging"))).thenReturn(new CompletableFuture<>());
		when(messageSender.send(anyString(), anyString(), eq("fine"))).thenReturn(acked());

		// when
		relay.relayPendingEvents();

		// then
		assertThat(hanging.getStatus()).isEqualTo(OutboxEventStatus.NEW);
		assertThat(hanging.getAttempts()).isEqualTo(1);
		assertThat(fine.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
	}

	@Test
	void shouldWaitForTheWholeBatchAgainstOneSharedDeadline() {
		// given
		givenBatch(message("x"), message("y"), message("z"));
		when(messageSender.send(anyString(), anyString(), anyString()))
				.thenAnswer(invocation -> new CompletableFuture<Void>());

		// when
		final long start = System.nanoTime();
		relay.relayPendingEvents();
		final Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

		// then
		assertThat(elapsed).isLessThan(SEND_TIMEOUT.multipliedBy(2));
	}

	@Test
	void shouldRecordFailureWhenSenderThrowsSynchronously() {
		// given
		final OutboxMessage broken = message("broken");
		final OutboxMessage fine = message("fine");
		givenBatch(broken, fine);
		when(messageSender.send(anyString(), anyString(), eq("broken")))
				.thenThrow(new RuntimeException("broker unavailable"));
		when(messageSender.send(anyString(), anyString(), eq("fine"))).thenReturn(acked());

		// when
		relay.relayPendingEvents();

		// then
		assertThat(broken.getAttempts()).isEqualTo(1);
		assertThat(fine.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
	}

	private void givenBatch(final OutboxMessage... messages) {
		when(outboxStore.lockNextPendingSkippingLocked(100)).thenReturn(List.of(messages));
	}

	private static CompletableFuture<Void> acked() {
		return CompletableFuture.completedFuture(null);
	}

	private static OutboxMessage message(final String payload) {
		final UUID eventId = UUID.randomUUID();
		return OutboxMessage.pending(
				UUID.randomUUID(), eventId, eventId.toString(), "TEST_EVENT", "topic", payload, NOW
		);
	}
}
