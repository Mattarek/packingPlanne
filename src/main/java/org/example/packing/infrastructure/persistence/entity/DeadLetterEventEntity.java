package org.example.packing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * One row per Kafka event that a consumer sent to its {@code .DLT} topic
 * (i.e. an event that exhausted its {@code @RetryableTopic} attempts, or
 * failed validation outright). Recorded here so the failure is visible
 * ({@link #getStatus()} / {@link #getReason()}) instead of only existing as
 * a log line, and so {@code DeadLetterRetryScheduler} has a payload it can
 * resubmit once the underlying problem (a bug, an outage) is fixed.
 */
@Entity
@Table(name = "dead_letter_events")
public class DeadLetterEventEntity {

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
			name = "topic_name",
			nullable = false,
			updatable = false
	)
	private String topicName;

	@Column(
			name = "payload",
			nullable = false,
			updatable = false,
			columnDefinition = "TEXT"
	)
	private String payload;

	@Column(
			name = "reason",
			nullable = false,
			columnDefinition = "TEXT"
	)
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(
			name = "status",
			nullable = false
	)
	private DeadLetterEventStatus status;

	@Column(
			name = "attempts",
			nullable = false
	)
	private int attempts;

	@Column(
			name = "first_failed_at",
			nullable = false,
			updatable = false
	)
	private Instant firstFailedAt;

	@Column(name = "last_failed_at", nullable = false)
	private Instant lastFailedAt;

	@Column(name = "last_retried_at")
	private Instant lastRetriedAt;

	protected DeadLetterEventEntity() {
	}

	public DeadLetterEventEntity(
			final UUID id,
			final UUID eventId,
			final String eventType,
			final String topicName,
			final String payload,
			final String reason,
			final Instant failedAt
	) {
		this.id = id;
		this.eventId = eventId;
		this.eventType = eventType;
		this.topicName = topicName;
		this.payload = payload;
		this.reason = reason;
		status = DeadLetterEventStatus.PENDING_RETRY;
		attempts = 0;
		firstFailedAt = failedAt;
		lastFailedAt = failedAt;
	}

	/**
	 * Called every time this same event lands back on the DLT (the initial
	 * failure, or another one after an automatic resubmission didn't stick).
	 * Once {@code maxAttempts} is reached, retrying automatically stops and
	 * the event is left for a human to look at.
	 */
	public void recordFailure(
			final String failureReason,
			final int maxAttempts,
			final Instant failedAt
	) {
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
