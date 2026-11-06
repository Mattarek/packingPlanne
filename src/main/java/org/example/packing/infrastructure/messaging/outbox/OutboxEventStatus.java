package org.example.packing.infrastructure.messaging.outbox;

public enum OutboxEventStatus {
	NEW,
	PUBLISHED,
	FAILED
}