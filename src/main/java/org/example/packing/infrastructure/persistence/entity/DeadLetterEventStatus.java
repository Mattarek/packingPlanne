package org.example.packing.infrastructure.persistence.entity;

public enum DeadLetterEventStatus {
	/** Sitting in the DLT, waiting for {@code DeadLetterRetryScheduler} to resubmit it. */
	PENDING_RETRY,

	/** Resubmitted onto the original topic; whether it now succeeds is up to the consumer. */
	RESUBMITTED,

	/** Resubmitted {@code app.kafka.dead-letter.max-attempts} times with no lasting success; needs a human. */
	ABANDONED
}
