package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.infrastructure.messaging.inbox.InboxStore;
import org.example.packing.infrastructure.persistence.repository.InboxEventRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JpaInboxStore implements InboxStore {

	private final InboxEventRepository repository;

	public JpaInboxStore(final InboxEventRepository repository) {
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
}
