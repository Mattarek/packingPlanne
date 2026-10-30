package org.example.packing.infrastructure.messaging.outbox;

import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
@ConditionalOnKafkaEnabled
public class OutboxRetentionCleaner {

	private static final Logger log = LoggerFactory.getLogger(OutboxRetentionCleaner.class);

	private final OutboxStore outboxStore;
	private final Duration retention;
	private final Clock clock;

	public OutboxRetentionCleaner(
			final OutboxStore outboxStore,
			final OutboxProperties properties,
			final Clock clock
	) {
		this.outboxStore = outboxStore;
		this.retention = properties.retention();
		this.clock = clock;
	}

	@Scheduled(fixedDelayString = "${app.kafka.outbox.cleanup-interval-ms}")
	@Transactional
	public void deleteExpiredPublishedEvents() {
		final Instant publishedBefore = clock.instant().minus(retention);

		final int deleted = outboxStore.deletePublishedBefore(publishedBefore);

		if (deleted > 0) {
			log.info("Deleted {} published outbox events older than {}", deleted, publishedBefore);
		}
	}
}
