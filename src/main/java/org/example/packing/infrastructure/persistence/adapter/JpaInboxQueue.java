package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.infrastructure.messaging.inbox.InboxMessage;
import org.example.packing.infrastructure.messaging.inbox.InboxQueue;
import org.example.packing.infrastructure.persistence.entity.InboxMessageEntity;
import org.example.packing.infrastructure.persistence.repository.InboxMessageRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class JpaInboxQueue implements InboxQueue {

	private final InboxMessageRepository repository;

	public JpaInboxQueue(final InboxMessageRepository repository) {
		this.repository = repository;
	}

	@Override
	public void enqueue(final InboxMessage message) {
		repository.insertIgnoringDuplicate(
				message.id(),
				message.eventId(),
				message.eventType(),
				message.eventVersion(),
				message.sourceTopic(),
				message.payload(),
				message.receivedAt()
		);
	}

	@Override
	public List<InboxMessage> claimAvailable(
			final int limit,
			final Instant now,
			final Instant leaseUntil,
			final UUID claimToken
	) {
		return repository.claimAvailable(limit, now, leaseUntil, claimToken).stream()
				.map(JpaInboxQueue::toMessage)
				.toList();
	}

	@Override
	public boolean deleteClaimed(final UUID id, final UUID claimToken) {
		return repository.deleteClaimed(id, claimToken) > 0;
	}

	@Override
	public boolean releaseForRetry(
			final UUID id,
			final UUID claimToken,
			final Instant availableAt,
			final String lastError
	) {
		return repository.releaseForRetry(id, claimToken, availableAt, lastError) > 0;
	}

	private static InboxMessage toMessage(final InboxMessageEntity entity) {
		return new InboxMessage(
				entity.getId(),
				entity.getEventId(),
				entity.getEventType(),
				entity.getEventVersion(),
				entity.getSourceTopic(),
				entity.getPayload(),
				entity.getReceivedAt(),
				entity.getAttempts(),
				entity.getClaimToken()
		);
	}
}
