package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PackageCreatedEventPublisherTest {

	private static final String TOPIC_NAME = "package-created-events";

	private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

	@Mock
	private OutboxEventRepository outboxEventRepository;

	private PackageCreatedEventPublisher publisher;

	@BeforeEach
	void setUp() {
		final ObjectMapper objectMapper = JsonMapper.builder().build();
		publisher = new PackageCreatedEventPublisher(
				outboxEventRepository,
				objectMapper,
				Clock.fixed(NOW, ZoneOffset.UTC),
				UUID::randomUUID,
				TOPIC_NAME
		);
	}

	@Test
	void shouldDoNothingWhenNoPackagesWereCreated() {
		// when
		publisher.publish(List.of());

		// then
		verifyNoInteractions(outboxEventRepository);
	}

	@Test
	void shouldSaveOutboxEventForCreatedPackages() {
		// given
		final Package pkg = new Package(
				UUID.randomUUID(), new Dimensions(10.0, 20.0, 30.0), new Weight(5.0),
				ProductCategory.FOOD, FragilityLevel.FRAGILE
		);

		// when
		publisher.publish(List.of(pkg));

		// then
		final ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
		verify(outboxEventRepository).save(captor.capture());

		final OutboxEventEntity saved = captor.getValue();
		assertThat(saved.getTopicName()).isEqualTo(TOPIC_NAME);
		assertThat(saved.getEventType()).isEqualTo("PACKAGE_CREATED");
		assertThat(saved.getAggregateId()).isEqualTo(saved.getEventId().toString());
		assertThat(saved.getCreatedAt()).isEqualTo(NOW);
		assertThat(saved.getPayload())
				.contains(pkg.id().toString())
				.contains("PACKAGE_CREATED")
				.contains("\"category\":\"FOOD\"")
				.contains("\"fragility\":\"FRAGILE\"");
	}

	/**
	 * Regression test: aggregateId used to join every package id in the batch
	 * with commas, which overflowed {@code outbox_events.aggregate_id}
	 * (VARCHAR(100)) for any batch of 3+ packages and silently rolled back
	 * the whole create-packages transaction. It must stay bounded (the
	 * event id) no matter how many packages are in one batch.
	 */
	@Test
	void shouldKeepAggregateIdBoundedRegardlessOfBatchSize() {
		// given: a batch large enough that the old "join every id" logic
		// would have overflowed a VARCHAR(100) column (40 UUIDs ~= 1479 chars)
		final List<Package> packages = IntStream.range(0, 40)
				.mapToObj(i -> new Package(
						UUID.randomUUID(), new Dimensions(10.0, 20.0, 30.0), new Weight(5.0)
				))
				.toList();

		// when
		publisher.publish(packages);

		// then
		final ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
		verify(outboxEventRepository).save(captor.capture());

		final String aggregateId = captor.getValue().getAggregateId();
		assertThat(aggregateId).hasSizeLessThanOrEqualTo(100);
		assertThat(aggregateId).isEqualTo(captor.getValue().getEventId().toString());
	}
}
