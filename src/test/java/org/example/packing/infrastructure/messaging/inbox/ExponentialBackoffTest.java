package org.example.packing.infrastructure.messaging.inbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExponentialBackoffTest {

	private final ExponentialBackoff backoff = new ExponentialBackoff(Duration.ofSeconds(1), Duration.ofMinutes(1));

	@ParameterizedTest
	@CsvSource({
			"1, 1000",
			"2, 2000",
			"3, 4000",
			"6, 32000",
			"7, 60000",
			"100, 60000",
			"2147483647, 60000"
	})
	void shouldDoubleDelayUpToTheCap(final int failedAttempts, final long expectedMillis) {
		assertThat(backoff.delayAfter(failedAttempts)).isEqualTo(Duration.ofMillis(expectedMillis));
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -1})
	void shouldRejectNonPositiveAttempts(final int failedAttempts) {
		assertThatThrownBy(() -> backoff.delayAfter(failedAttempts))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectMaxShorterThanInitial() {
		assertThatThrownBy(() -> new ExponentialBackoff(Duration.ofSeconds(10), Duration.ofSeconds(1)))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNonPositiveInitial() {
		assertThatThrownBy(() -> new ExponentialBackoff(Duration.ZERO, Duration.ofSeconds(1)))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
