package org.example.packing.infrastructure.messaging.outbox;

import java.time.Instant;
import java.util.UUID;

public final class OutboxMessage {

	private final UUID id;
	private final UUID eventId;
	private final String aggregateId;
	private final String eventType;
	private final String topicName;
	private final String payload;
	private final Instant createdAt;
	private OutboxEventStatus status;
	private Instant publishedAt;
	private int attempts;

	public OutboxMessage(
			final UUID id,
			final UUID eventId,
			final String aggregateId,
			final String eventType,
			final String topicName,
			final String payload,
			final Instant createdAt,
			final OutboxEventStatus status,
			final Instant publishedAt,
			final int attempts
	) {
		this.id = id;
		this.eventId = eventId;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.topicName = topicName;
		this.payload = payload;
		this.createdAt = createdAt;
		this.status = status;
		this.publishedAt = publishedAt;
		this.attempts = attempts;
	}

	public static OutboxMessage pending(
			final UUID id,
			final UUID eventId,
			final String aggregateId,
			final String eventType,
			final String topicName,
			final String payload,
			final Instant createdAt
	) {
		return new OutboxMessage(
				id, eventId, aggregateId, eventType, topicName, payload, createdAt,
				OutboxEventStatus.NEW, null, 0
		);
	}

	public void markPublished(final Instant publishedAt) {
		status = OutboxEventStatus.PUBLISHED;
		this.publishedAt = publishedAt;
	}

	public void recordFailedAttempt(final int maxAttempts) {
		attempts++;
		if (attempts >= maxAttempts) {
			status = OutboxEventStatus.FAILED;
		}
	}

	public boolean isFailed() {
		return status == OutboxEventStatus.FAILED;
	}

	public UUID getId() {
		return id;
	}

	public UUID getEventId() {
		return eventId;
	}

	public String getAggregateId() {
		return aggregateId;
	}

	public String getEventType() {
		return eventType;
	}

	public String getTopicName() {
		return topicName;
	}

	public String getPayload() {
		return payload;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public OutboxEventStatus getStatus() {
		return status;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public int getAttempts() {
		return attempts;
	}
}
