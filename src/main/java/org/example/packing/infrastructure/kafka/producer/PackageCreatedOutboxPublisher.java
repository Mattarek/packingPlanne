package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.application.port.out.IdGenerator;
import org.example.packing.application.port.out.PackageCreatedPublisher;
import org.example.packing.domain.model.Package;
import org.example.packing.infrastructure.kafka.config.KafkaTopicsProperties;
import org.example.packing.infrastructure.kafka.event.EventEnumMapper;
import org.example.packing.infrastructure.kafka.event.PackageCreatedEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreatedItem;
import org.example.packing.infrastructure.messaging.PayloadSerializer;
import org.example.packing.infrastructure.messaging.outbox.AbstractOutboxEventPublisher;
import org.example.packing.infrastructure.messaging.outbox.OutboxStore;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class PackageCreatedOutboxPublisher
		extends AbstractOutboxEventPublisher<List<Package>>
		implements PackageCreatedPublisher {

	private static final String EVENT_TYPE = "PACKAGE_CREATED";

	private static final int VERSION = 1;

	private final String topicName;

	public PackageCreatedOutboxPublisher(
			final OutboxStore outboxStore,
			final PayloadSerializer payloadSerializer,
			final Clock clock,
			final IdGenerator idGenerator,
			final KafkaTopicsProperties topics
	) {
		super(outboxStore, payloadSerializer, clock, idGenerator);
		this.topicName = topics.packageCreatedEvents();
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
