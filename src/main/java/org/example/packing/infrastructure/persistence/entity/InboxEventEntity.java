package org.example.packing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inbox_events")
public class InboxEventEntity {

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
			name = "event_type",
			nullable = false,
			updatable = false
	)
	private String eventType;

	@Column(
			name = "received_at",
			nullable = false,
			updatable = false
	)
	private Instant receivedAt;

	protected InboxEventEntity() {
	}

	public InboxEventEntity(
			final UUID id,
			final UUID eventId,
			final String eventType,
			final Instant receivedAt
	) {
		this.id = id;
		this.eventId = eventId;
		this.eventType = eventType;
		this.receivedAt = receivedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getEventId() {
		return eventId;
	}

	public String getEventType() {
		return eventType;
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}
}