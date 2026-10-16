package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.application.port.IdGenerator;
import org.example.packing.application.port.PackageCreatedPublisher;
import org.example.packing.domain.model.Package;
import org.example.packing.infrastructure.kafka.event.EventEnumMapper;
import org.example.packing.infrastructure.kafka.event.PackageCreatedEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreatedItem;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Kafka (outbox) implementation of the application's {@link PackageCreatedPublisher}.
 * The event is built from the domain {@link Package}, not from the REST response.
 */
@Component
public class PackageCreatedEventPublisher
		extends AbstractKafkaEventPublisher<List<Package>>
		implements PackageCreatedPublisher {

	private static final String EVENT_TYPE = "PACKAGE_CREATED";

	private static final int VERSION = 1;

	private final String topicName;

	public PackageCreatedEventPublisher(
			final OutboxEventRepository outboxEventRepository,
			final ObjectMapper objectMapper,
			final Clock clock,
			final IdGenerator idGenerator,
			@Value("${app.kafka.topics.package-created-events}") final String topicName
	) {
		super(outboxEventRepository, objectMapper, clock, idGenerator);
		this.topicName = topicName;
	}

	@Override
	protected boolean shouldSkip(final List<Package> payload) {
		return payload == null || payload.isEmpty();
	}

	@Override
	protected Object buildEnvelope(
			final UUID eventId,
			final Instant occurredAt,
			final List<Package> payload
	) {
		return new PackageCreatedEvent(eventId, EVENT_TYPE, VERSION, occurredAt, toItems(payload));
	}

	@Override
	protected String eventType() {
		return EVENT_TYPE;
	}

	@Override
	protected String topicName() {
		return topicName;
	}

	private List<PackageCreatedItem> toItems(final List<Package> packages) {
		return packages.stream()
				.map(pkg -> new PackageCreatedItem(
						pkg.id(),
						pkg.dimensions().length(),
						pkg.dimensions().width(),
						pkg.dimensions().height(),
						pkg.weight().kilograms(),
						EventEnumMapper.toWire(pkg.category()),
						EventEnumMapper.toWire(pkg.fragility())
				))
				.toList();
	}
}
