package org.example.packing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inbox_messages")
public class InboxMessageEntity {

	@Id
	private UUID id;

	@Column(name = "event_id", nullable = false, unique = true, updatable = false)
	private UUID eventId;

	@Column(name = "event_type", nullable = false, updatable = false)
	private String eventType;

	@Column(name = "event_version", nullable = false, updatable = false)
	private int eventVersion;

	@Column(name = "source_topic", nullable = false, updatable = false)
	private String sourceTopic;

	@Column(name = "payload", nullable = false, updatable = false)
	private String payload;

	@Column(name = "received_at", nullable = false, updatable = false)
	private Instant receivedAt;

	@Column(name = "available_at", nullable = false)
	private Instant availableAt;

	@Column(name = "attempts", nullable = false)
	private int attempts;

	@Column(name = "claim_token")
	private UUID claimToken;

	@Column(name = "last_error")
	private String lastError;

	protected InboxMessageEntity() {
	}

	public InboxMessageEntity(
			final UUID id,
			final UUID eventId,
			final String eventType,
			final int eventVersion,
			final String sourceTopic,
			final String payload,
			final Instant receivedAt,
			final Instant availableAt,
			final int attempts,
			final UUID claimToken,
			final String lastError
	) {
		this.id = id;
		this.eventId = eventId;
		this.eventType = eventType;
		this.eventVersion = eventVersion;
		this.sourceTopic = sourceTopic;
		this.payload = payload;
		this.receivedAt = receivedAt;
		this.availableAt = availableAt;
		this.attempts = attempts;
		this.claimToken = claimToken;
		this.lastError = lastError;
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

	public int getEventVersion() {
		return eventVersion;
	}

	public String getSourceTopic() {
		return sourceTopic;
	}

	public String getPayload() {
		return payload;
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}

	public Instant getAvailableAt() {
		return availableAt;
	}

	public int getAttempts() {
		return attempts;
	}

	public UUID getClaimToken() {
		return claimToken;
	}

	public String getLastError() {
		return lastError;
	}
}
