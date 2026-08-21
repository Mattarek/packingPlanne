package org.example.packing.infrastructure.kafka.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PackageCreatedEvent(
		UUID eventId,
		String eventType,
		int version,
		Instant occurredAt,
		List<PackageCreatedItem> packages
) {
}
