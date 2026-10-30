package org.example.packing.infrastructure.messaging.inbox;

import org.example.packing.application.port.out.IdGenerator;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
public class InboxDeduplicator {

	private final InboxStore inboxStore;
	private final IdGenerator idGenerator;
	private final Clock clock;

	public InboxDeduplicator(
			final InboxStore inboxStore,
			final IdGenerator idGenerator,
			final Clock clock
	) {
		this.inboxStore = inboxStore;
		this.idGenerator = idGenerator;
		this.clock = clock;
	}

	public boolean registerFirstDelivery(final UUID eventId, final String eventType) {
		return inboxStore.registerIfAbsent(idGenerator.newId(), eventId, eventType, clock.instant());
	}
}
