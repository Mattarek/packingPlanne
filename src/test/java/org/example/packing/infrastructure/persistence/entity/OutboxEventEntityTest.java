package org.example.packing.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S6: the entity owns its state transitions (tell, don't ask).
 */
class OutboxEventEntityTest {

	private static final int MAX_ATTEMPTS = 3;

	@Test
	void shouldStayNewWhileAttemptsAreBelowTheLimit() {
		// given
		final OutboxEventEntity event = newEvent();

		// when
		event.recordFailedAttempt(MAX_ATTEMPTS);
		event.recordFailedAttempt(MAX_ATTEMPTS);

		// then
		assertThat(event.getAttempts()).isEqualTo(2);
		assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.NEW);
		assertThat(event.isFailed()).isFalse();
	}

	@Test
	void shouldBecomeFailedWhenTheLimitIsReached() {
		// given
		final OutboxEventEntity event = newEvent();

		// when
		event.recordFailedAttempt(MAX_ATTEMPTS);
		event.recordFailedAttempt(MAX_ATTEMPTS);
		event.recordFailedAttempt(MAX_ATTEMPTS);

		// then
		assertThat(event.getAttempts()).isEqualTo(3);
		assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
		assertThat(event.isFailed()).isTrue();
	}

	@Test
	void shouldRecordTheGivenPublishTime() {
		// given: D4 – time is passed in, so it is assertable
		final OutboxEventEntity event = newEvent();
		final Instant publishedAt = Instant.parse("2026-05-05T05:05:05Z");

		// when
		event.markPublished(publishedAt);

		// then
		assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
		assertThat(event.getPublishedAt()).isEqualTo(publishedAt);
	}

	private static OutboxEventEntity newEvent() {
		return new OutboxEventEntity(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"aggregate",
				"TEST_EVENT",
				"topic",
				"{}",
				Instant.parse("2026-01-01T00:00:00Z")
		);
	}
}
