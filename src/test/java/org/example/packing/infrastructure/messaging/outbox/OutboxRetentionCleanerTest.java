package org.example.packing.infrastructure.messaging.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxRetentionCleanerTest {

	@Mock
	private OutboxStore outboxStore;

	@Test
	void shouldDeletePublishedMessagesOlderThanRetention() {
		// given
		final OutboxRetentionCleaner cleaner = new OutboxRetentionCleaner(
				outboxStore,
				new OutboxProperties(100, 5, Duration.ofSeconds(35), Duration.ofDays(7)),
				Clock.fixed(Instant.parse("2026-06-08T00:00:00Z"), ZoneOffset.UTC)
		);

		// when
		cleaner.deleteExpiredPublishedEvents();

		// then
		verify(outboxStore).deletePublishedBefore(Instant.parse("2026-06-01T00:00:00Z"));
	}
}
