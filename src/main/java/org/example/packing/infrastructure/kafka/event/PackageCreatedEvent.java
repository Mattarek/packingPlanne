package org.example.packing.infrastructure.kafka.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published (via the transactional outbox — see
 * {@link org.example.packing.infrastructure.kafka.producer.PackageCreatedEventPublisher})
 * whenever one or more packages are successfully created, regardless of
 * whether they came in through the REST API or the
 * {@code package-create-requests} Kafka topic — both paths converge on
 * {@code PackageService.createPackages}.
 */
public record PackageCreatedEvent(
		UUID eventId,
		String eventType,
		int version,
		Instant occurredAt,
		List<PackageCreatedItem> packages
) {
}
