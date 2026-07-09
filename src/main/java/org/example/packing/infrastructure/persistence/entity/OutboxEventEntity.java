package org.example.packing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

	@Id
	private UUID id;

	@Column(
			name = "event_id",
			nullable = false,
			unique = true,
			updatable = false
	)
	private UUID eventId;

	@Column(
			name = "aggregate_id",
			nullable = false,
			updatable = false
	)
	private String aggregateId;

	@Column(
			name = "event_type",
			nullable = false,
			updatable = false
	)
	private String eventType;

	@Column(
			name = "topic_name",
			nullable = false,
			updatable = false
	)
	private String topicName;

	@Column(
			name = "payload",
			nullable = false,
			columnDefinition = "TEXT"
	)
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(
			name = "status",
			nullable = false
	)
	private OutboxEventStatus status;

	@Column(
			name = "created_at",
			nullable = false,
			updatable = false
	)
	private Instant createdAt;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(
			name = "attempts",
			nullable = false
	)
	private int attempts;

	protected OutboxEventEntity() {
	}

	public OutboxEventEntity(
			final UUID id,
			final UUID eventId,
			final String aggregateId,
			final String eventType,
			final String topicName,
			final String payload,
			final Instant createdAt
	) {
		this.id = id;
		this.eventId = eventId;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.topicName = topicName;
		this.payload = payload;
		status = OutboxEventStatus.NEW;
		this.createdAt = createdAt;
		attempts = 0;
	}

	public void markPublished() {
		status = OutboxEventStatus.PUBLISHED;
		publishedAt = Instant.now();
	}

	public void markFailed() {
		status = OutboxEventStatus.FAILED;
		attempts++;
	}

	public void increaseAttempts() {
		attempts++;
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

	public OutboxEventStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public int getAttempts() {
		return attempts;
	}
}