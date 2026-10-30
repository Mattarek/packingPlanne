package org.example.packing.infrastructure.persistence.adapter;

import org.example.packing.infrastructure.messaging.deadletter.DeadLetter;
import org.example.packing.infrastructure.messaging.deadletter.DeadLetterEventStatus;
import org.example.packing.infrastructure.messaging.deadletter.DeadLetterStore;
import org.example.packing.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.example.packing.infrastructure.persistence.repository.DeadLetterEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaDeadLetterStore implements DeadLetterStore {

	private final DeadLetterEventRepository repository;

	public JpaDeadLetterStore(final DeadLetterEventRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<DeadLetter> findByEventId(final UUID eventId) {
		return repository.findByEventId(eventId).map(JpaDeadLetterStore::toDeadLetter);
	}

	@Override
	public List<DeadLetter> lockNextPendingRetrySkippingLocked(final int limit) {
		return repository
				.findNextBatchSkippingLocked(DeadLetterEventStatus.PENDING_RETRY, PageRequest.of(0, limit))
				.stream()
				.map(JpaDeadLetterStore::toDeadLetter)
				.toList();
	}

	@Override
	public void save(final DeadLetter deadLetter) {
		repository.save(toEntity(deadLetter));
	}

	private static DeadLetterEventEntity toEntity(final DeadLetter deadLetter) {
		return new DeadLetterEventEntity(
				deadLetter.getId(),
				deadLetter.getEventId(),
				deadLetter.getEventType(),
				deadLetter.getTopicName(),
				deadLetter.getPayload(),
				deadLetter.getReason(),
				deadLetter.getStatus(),
				deadLetter.getAttempts(),
				deadLetter.getFirstFailedAt(),
				deadLetter.getLastFailedAt(),
				deadLetter.getLastRetriedAt()
		);
	}

	private static DeadLetter toDeadLetter(final DeadLetterEventEntity entity) {
		return new DeadLetter(
				entity.getId(),
				entity.getEventId(),
				entity.getEventType(),
				entity.getTopicName(),
				entity.getPayload(),
				entity.getReason(),
				entity.getStatus(),
				entity.getAttempts(),
				entity.getFirstFailedAt(),
				entity.getLastFailedAt(),
				entity.getLastRetriedAt()
		);
	}
}
