package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.infrastructure.messaging.outbox.OutboxEventStatus;
import org.example.packing.infrastructure.messaging.outbox.OutboxMessage;
import org.example.packing.infrastructure.messaging.outbox.OutboxStore;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class JpaOutboxStore implements OutboxStore {

	private final OutboxEventRepository repository;

	public JpaOutboxStore(final OutboxEventRepository repository) {
		this.repository = repository;
	}

	@Override
	public void add(final OutboxMessage message) {
		repository.save(toEntity(message));
	}

	@Override
	public List<OutboxMessage> lockNextPendingSkippingLocked(final int limit) {
		return repository.findNextBatchSkippingLocked(OutboxEventStatus.NEW, PageRequest.of(0, limit))
				.stream()
				.map(JpaOutboxStore::toMessage)
				.toList();
	}

	@Override
	public void saveAll(final List<OutboxMessage> messages) {
		repository.saveAll(messages.stream().map(JpaOutboxStore::toEntity).toList());
	}

	@Override
	public int deletePublishedBefore(final Instant publishedBefore) {
		return repository.deleteByStatusAndPublishedAtBefore(OutboxEventStatus.PUBLISHED, publishedBefore);
	}

	private static OutboxEventEntity toEntity(final OutboxMessage message) {
		return new OutboxEventEntity(
				message.getId(),
				message.getEventId(),
				message.getAggregateId(),
				message.getEventType(),
				message.getTopicName(),
				message.getPayload(),
				message.getCreatedAt(),
				message.getStatus(),
				message.getPublishedAt(),
				message.getAttempts()
		);
	}

	private static OutboxMessage toMessage(final OutboxEventEntity entity) {
		return new OutboxMessage(
				entity.getId(),
				entity.getEventId(),
				entity.getAggregateId(),
				entity.getEventType(),
				entity.getTopicName(),
				entity.getPayload(),
				entity.getCreatedAt(),
				entity.getStatus(),
				entity.getPublishedAt(),
				entity.getAttempts()
		);
	}
}
