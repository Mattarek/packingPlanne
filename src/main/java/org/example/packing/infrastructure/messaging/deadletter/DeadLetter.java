package org.example.packing.infrastructure.messaging.deadletter;

import java.time.Instant;
import java.util.UUID;

public final class DeadLetter {

	private final UUID id;
	private final UUID eventId;
	private final String eventType;
	private final String topicName;
	private final String payload;
	private final Instant firstFailedAt;
	private String reason;
	private DeadLetterEventStatus status;
	private int attempts;
	private Instant lastFailedAt;
	private Instant lastRetriedAt;

	public DeadLetter(
			final UUID id,
			final UUID eventId,
			final String eventType,
			final String topicName,
			final String payload,
			final String reason,
			final DeadLetterEventStatus status,
			final int attempts,
			final Instant firstFailedAt,
			final Instant lastFailedAt,
			final Instant lastRetriedAt
	) {
		this.id = id;
		this.eventId = eventId;
		this.eventType = eventType;
		this.topicName = topicName;
		this.payload = payload;
		this.reason = reason;
		this.status = status;
		this.attempts = attempts;
		this.firstFailedAt = firstFailedAt;
		this.lastFailedAt = lastFailedAt;
		this.lastRetriedAt = lastRetriedAt;
	}

	public static DeadLetter firstFailure(
			final UUID id,
			final UUID eventId,
			final String eventType,
			final String topicName,
			final String payload,
			final String reason,
			final Instant failedAt
	) {
		return new DeadLetter(
				id, eventId, eventType, topicName, payload, reason,
				DeadLetterEventStatus.PENDING_RETRY, 0, failedAt, failedAt, null
		);
	}

	public void recordFailure(final String failureReason, final int maxAttempts, final Instant failedAt) {
		reason = failureReason;
		lastFailedAt = failedAt;
		attempts++;
		status = attempts >= maxAttempts
				? DeadLetterEventStatus.ABANDONED
				: DeadLetterEventStatus.PENDING_RETRY;
	}

	public void markResubmitted(final Instant retriedAt) {
		status = DeadLetterEventStatus.RESUBMITTED;
		lastRetriedAt = retriedAt;
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

	public String getTopicName() {
		return topicName;
	}

	public String getPayload() {
		return payload;
	}

	public String getReason() {
		return reason;
	}

	public DeadLetterEventStatus getStatus() {
		return status;
	}

	public int getAttempts() {
		return attempts;
	}

	public Instant getFirstFailedAt() {
		return firstFailedAt;
	}

	public Instant getLastFailedAt() {
		return lastFailedAt;
	}

	public Instant getLastRetriedAt() {
		return lastRetriedAt;
	}
}
