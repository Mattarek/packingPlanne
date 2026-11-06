package org.example.packing.infrastructure.messaging.inbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboxLedgerCleanerTest {

	private static final Instant NOW = Instant.parse("2026-04-04T04:04:04Z");
	private static final Duration RETENTION = Duration.ofDays(7);
	private static final int CHUNK = 100;

	@Mock
	private InboxLedger inboxLedger;

	private InboxLedgerCleaner cleaner;

	@BeforeEach
	void setUp() {
		cleaner = new InboxLedgerCleaner(
				inboxLedger,
				new InboxProperties(
						10, 1, Duration.ofMinutes(5), 5,
						Duration.ofSeconds(1), Duration.ofMinutes(1), RETENTION, CHUNK
				),
				Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	@Test
	void shouldDeleteInChunksUntilAPartialChunkIsReturned() {
		// given
		final Instant cutoff = NOW.minus(RETENTION);
		when(inboxLedger.deleteReceivedBefore(cutoff, CHUNK)).thenReturn(CHUNK, CHUNK, 7);

		// when
		cleaner.deleteExpiredEntries();

		// then
		verify(inboxLedger, times(3)).deleteReceivedBefore(cutoff, CHUNK);
	}

	@Test
	void shouldStopAfterSingleCallWhenNothingIsExpired() {
		// given
		final Instant cutoff = NOW.minus(RETENTION);
		when(inboxLedger.deleteReceivedBefore(cutoff, CHUNK)).thenReturn(0);

		// when
		cleaner.deleteExpiredEntries();

		// then
		verify(inboxLedger, times(1)).deleteReceivedBefore(cutoff, CHUNK);
	}
}
