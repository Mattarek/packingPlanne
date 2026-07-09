package org.example.packing.infrastructure.persistence.entity;

public enum OutboxEventStatus {
	NEW,
	PUBLISHED,
	FAILED
}