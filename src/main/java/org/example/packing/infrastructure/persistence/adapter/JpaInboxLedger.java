package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.infrastructure.messaging.inbox.InboxLedger;
import org.example.packing.infrastructure.persistence.repository.InboxEventRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JpaInboxLedger implements InboxLedger {

	private final InboxEventRepository repository;

	public JpaInboxLedger(final InboxEventRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean registerIfAbsent(
			final UUID id,
			final UUID eventId,
			final String eventType,
			final Instant receivedAt
	) {
		return repository.insertIgnoringDuplicate(id, eventId, eventType, receivedAt) > 0;
	}

	@Override
	public void forget(final UUID eventId) {
		repository.deleteByEventIdNative(eventId);
	}

	@Override
	public int deleteReceivedBefore(final Instant receivedBefore, final int limit) {
		return repository.deleteChunkReceivedBefore(receivedBefore, limit);
	}
}
