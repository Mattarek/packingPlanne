package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.application.dto.PackageResponse;
import org.example.packing.infrastructure.kafka.event.PackageCreatedEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreatedItem;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Publishes a {@link PackageCreatedEvent} whenever one or more packages are
 * successfully created — see {@link AbstractKafkaEventPublisher} for the
 * shared outbox-writing mechanics.
 * <p>
 * Deliberately <strong>not</strong> gated by {@code app.kafka.enabled}: it
 * only ever writes to the {@code outbox_events} table, never talks to Kafka
 * directly, so it stays safe to call even when Kafka/the relay are disabled
 * — the row simply sits as {@code NEW} until a relay picks it up.
 */
@Component
public class PackageCreatedEventPublisher extends AbstractKafkaEventPublisher<List<PackageResponse>> {

	private static final String EVENT_TYPE = "PACKAGE_CREATED";

	private static final int VERSION = 1;

	private final String topicName;

	public PackageCreatedEventPublisher(
			final OutboxEventRepository outboxEventRepository,
			final ObjectMapper objectMapper,
			@Value("${app.kafka.topics.package-created-events}") final String topicName
	) {
		super(outboxEventRepository, objectMapper);
		this.topicName = topicName;
	}

	@Override
	protected boolean shouldSkip(final List<PackageResponse> payload) {
		return payload == null || payload.isEmpty();
	}

	@Override
	protected Object buildEnvelope(
			final UUID eventId,
			final Instant occurredAt,
			final List<PackageResponse> payload
	) {
		return new PackageCreatedEvent(eventId, EVENT_TYPE, VERSION, occurredAt, toItems(payload));
	}

	// aggregateId intentionally not overridden: a batch can contain any
	// number of packages, so there's no single natural key, and joining all
	// their ids would overflow outbox_events.aggregate_id (VARCHAR(100)) —
	// see AbstractKafkaEventPublisher's default. Every package's id is
	// already in the JSON payload itself if needed.

	@Override
	protected String eventType() {
		return EVENT_TYPE;
	}

	@Override
	protected String topicName() {
		return topicName;
	}

	private List<PackageCreatedItem> toItems(final List<PackageResponse> responses) {
		return responses.stream()
				.map(response -> new PackageCreatedItem(
						response.id(),
						response.length(),
						response.width(),
						response.height(),
						response.weight(),
						response.category(),
						response.fragility()
				))
				.toList();
	}
}
