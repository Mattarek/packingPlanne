package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.application.port.out.IdGenerator;
import org.example.packing.application.port.out.PackageCreatedPublisher;
import org.example.packing.domain.model.Package;
import org.example.packing.infrastructure.kafka.config.KafkaTopicsProperties;
import org.example.packing.infrastructure.kafka.event.EventContract;
import org.example.packing.infrastructure.kafka.event.EventType;
import org.example.packing.infrastructure.kafka.event.FragilityLevelWire;
import org.example.packing.infrastructure.kafka.event.PackageCreatedEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreatedItem;
import org.example.packing.infrastructure.kafka.event.ProductCategoryWire;
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

	private static final EventContract CONTRACT = EventContract.v1(EventType.PACKAGE_CREATED);

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
		return new PackageCreatedEvent(eventId, eventType(), CONTRACT.version(), occurredAt, toItems(payload));
	}

	@Override
	protected String eventType() {
		return CONTRACT.type().wireValue();
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
						ProductCategoryWire.fromDomain(pkg.category()).wireValue(),
						FragilityLevelWire.fromDomain(pkg.fragility()).wireValue()
				))
				.toList();
	}
}
