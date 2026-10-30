package org.example.packing.infrastructure.messaging.outbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxMessageTest {

	private static final int MAX_ATTEMPTS = 3;

	@Test
	void shouldStartAsPendingWithoutAttempts() {
		// when
		final OutboxMessage message = newMessage();

		// then
		assertThat(message.getStatus()).isEqualTo(OutboxEventStatus.NEW);
		assertThat(message.getAttempts()).isZero();
		assertThat(message.getPublishedAt()).isNull();
	}

	@Test
	void shouldStayNewWhileAttemptsAreBelowTheLimit() {
		// given
		final OutboxMessage message = newMessage();

		// when
		message.recordFailedAttempt(MAX_ATTEMPTS);
		message.recordFailedAttempt(MAX_ATTEMPTS);

		// then
		assertThat(message.getAttempts()).isEqualTo(2);
		assertThat(message.getStatus()).isEqualTo(OutboxEventStatus.NEW);
		assertThat(message.isFailed()).isFalse();
	}

	@Test
	void shouldBecomeFailedWhenTheLimitIsReached() {
		// given
		final OutboxMessage message = newMessage();

		// when
		message.recordFailedAttempt(MAX_ATTEMPTS);
		message.recordFailedAttempt(MAX_ATTEMPTS);
		message.recordFailedAttempt(MAX_ATTEMPTS);

		// then
		assertThat(message.getAttempts()).isEqualTo(3);
		assertThat(message.isFailed()).isTrue();
	}

	@Test
	void shouldRecordTheGivenPublishTime() {
		// given
		final OutboxMessage message = newMessage();
		final Instant publishedAt = Instant.parse("2026-05-05T05:05:05Z");

		// when
		message.markPublished(publishedAt);

		// then
		assertThat(message.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
		assertThat(message.getPublishedAt()).isEqualTo(publishedAt);
	}

	private static OutboxMessage newMessage() {
		return OutboxMessage.pending(
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
