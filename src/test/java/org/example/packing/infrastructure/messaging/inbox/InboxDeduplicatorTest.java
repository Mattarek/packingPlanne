package org.example.packing.infrastructure.messaging.inbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboxDeduplicatorTest {

	private static final Instant NOW = Instant.parse("2026-04-04T04:04:04Z");
	private static final UUID ROW_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

	@Mock
	private InboxStore inboxStore;

	private InboxDeduplicator deduplicator;

	@BeforeEach
	void setUp() {
		deduplicator = new InboxDeduplicator(inboxStore, () -> ROW_ID, Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void shouldRegisterFirstDeliveryWithExactIdAndTimestamp() {
		// given
		final UUID eventId = UUID.randomUUID();
		when(inboxStore.registerIfAbsent(ROW_ID, eventId, "TYPE", NOW)).thenReturn(true);

		// when & then
		assertThat(deduplicator.registerFirstDelivery(eventId, "TYPE")).isTrue();
	}

	@Test
	void shouldReportDuplicateWhenStoreAlreadyHasTheEvent() {
		// given
		final UUID eventId = UUID.randomUUID();
		when(inboxStore.registerIfAbsent(ROW_ID, eventId, "TYPE", NOW)).thenReturn(false);

		// when & then
		assertThat(deduplicator.registerFirstDelivery(eventId, "TYPE")).isFalse();
	}
}
