package org.example.packing.infrastructure.messaging.inbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboxAcceptorTest {

	private static final Instant NOW = Instant.parse("2026-04-04T04:04:04Z");
	private static final UUID ROW_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

	@Mock
	private InboxLedger inboxLedger;

	@Mock
	private InboxQueue inboxQueue;

	private InboxAcceptor acceptor;

	@BeforeEach
	void setUp() {
		acceptor = new InboxAcceptor(inboxLedger, inboxQueue, () -> ROW_ID, Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void shouldRegisterAndEnqueueFirstDelivery() {
		// given
		final IncomingMessage incoming = incoming();
		when(inboxLedger.registerIfAbsent(ROW_ID, incoming.eventId(), "TYPE", NOW)).thenReturn(true);

		// when
		final boolean accepted = acceptor.accept(incoming);

		// then
		assertThat(accepted).isTrue();
		verify(inboxQueue).enqueue(new InboxMessage(
				ROW_ID, incoming.eventId(), "TYPE", 1, "topic", "{}", NOW, 0, null
		));
	}

	@Test
	void shouldNotEnqueueDuplicateDelivery() {
		// given
		final IncomingMessage incoming = incoming();
		when(inboxLedger.registerIfAbsent(ROW_ID, incoming.eventId(), "TYPE", NOW)).thenReturn(false);

		// when
		final boolean accepted = acceptor.accept(incoming);

		// then
		assertThat(accepted).isFalse();
		verifyNoInteractions(inboxQueue);
	}

	private static IncomingMessage incoming() {
		return new IncomingMessage(UUID.randomUUID(), "TYPE", 1, "topic", "{}");
	}
}
