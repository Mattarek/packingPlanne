package org.example.packing.infrastructure.kafka.inbox;

import org.example.packing.infrastructure.persistence.repository.InboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboxDeduplicatorTest {

	private static final Instant NOW = Instant.parse("2026-04-04T04:04:04Z");
	private static final UUID ROW_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

	@Mock
	private InboxEventRepository repository;

	@Test
	void shouldRegisterFirstDeliveryWithExactIdAndTimestamp() {
		// given
		final InboxDeduplicator deduplicator =
				new InboxDeduplicator(repository, () -> ROW_ID, Clock.fixed(NOW, ZoneOffset.UTC));
		final UUID eventId = UUID.randomUUID();
		when(repository.insertIgnoringDuplicate(ROW_ID, eventId, "TYPE", NOW)).thenReturn(1);

		// when
		final boolean first = deduplicator.registerFirstDelivery(eventId, "TYPE");

		// then
		assertThat(first).isTrue();
		verify(repository).insertIgnoringDuplicate(ROW_ID, eventId, "TYPE", NOW);
	}

	@Test
	void shouldReportDuplicateWhenNothingWasInserted() {
		// given
		final InboxDeduplicator deduplicator =
				new InboxDeduplicator(repository, () -> ROW_ID, Clock.fixed(NOW, ZoneOffset.UTC));
		final UUID eventId = UUID.randomUUID();
		when(repository.insertIgnoringDuplicate(ROW_ID, eventId, "TYPE", NOW)).thenReturn(0);

		// when & then
		assertThat(deduplicator.registerFirstDelivery(eventId, "TYPE")).isFalse();
	}
}
