package org.example.packing.infrastructure.messaging.inbox;

import org.example.packing.application.port.out.IdGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Component
public class InboxAcceptor {

	private final InboxLedger inboxLedger;
	private final InboxQueue inboxQueue;
	private final IdGenerator idGenerator;
	private final Clock clock;

	public InboxAcceptor(
			final InboxLedger inboxLedger,
			final InboxQueue inboxQueue,
			final IdGenerator idGenerator,
			final Clock clock
	) {
		this.inboxLedger = inboxLedger;
		this.inboxQueue = inboxQueue;
		this.idGenerator = idGenerator;
		this.clock = clock;
	}

	@Transactional
	public boolean accept(final IncomingMessage incoming) {
		final Instant receivedAt = clock.instant();

		if (!inboxLedger.registerIfAbsent(idGenerator.newId(), incoming.eventId(), incoming.eventType(), receivedAt)) {
			return false;
		}

		inboxQueue.enqueue(InboxMessage.received(idGenerator.newId(), incoming, receivedAt));
		return true;
	}
}
