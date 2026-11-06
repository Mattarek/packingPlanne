package org.example.packing.infrastructure.messaging.inbox;

import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
@ConditionalOnKafkaEnabled
public class InboxLedgerCleaner {

	private static final Logger log = LoggerFactory.getLogger(InboxLedgerCleaner.class);

	private final InboxLedger inboxLedger;
	private final Duration retention;
	private final int chunkSize;
	private final Clock clock;

	public InboxLedgerCleaner(
			final InboxLedger inboxLedger,
			final InboxProperties properties,
			final Clock clock
	) {
		this.inboxLedger = inboxLedger;
		this.retention = properties.ledgerRetention();
		this.chunkSize = properties.cleanupBatchSize();
		this.clock = clock;
	}

	@Scheduled(fixedDelayString = "${app.kafka.inbox.cleanup-interval-ms}")
	public void deleteExpiredEntries() {
		final Instant receivedBefore = clock.instant().minus(retention);

		long total = 0;
		int deleted;
		do {
			deleted = inboxLedger.deleteReceivedBefore(receivedBefore, chunkSize);
			total += deleted;
		} while (deleted == chunkSize && !Thread.currentThread().isInterrupted());

		if (total > 0) {
			log.info("Deleted {} inbox ledger entries received before {}", total, receivedBefore);
		}
	}
}
