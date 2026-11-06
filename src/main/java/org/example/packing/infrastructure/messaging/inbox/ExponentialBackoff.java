package org.example.packing.infrastructure.messaging.inbox;

import java.time.Duration;
import java.util.Objects;

public record ExponentialBackoff(Duration initial, Duration max) {

	private static final int MAX_SHIFT = 30;

	public ExponentialBackoff {
		Objects.requireNonNull(initial, "initial must not be null");
		Objects.requireNonNull(max, "max must not be null");
		if (initial.isNegative() || initial.isZero()) {
			throw new IllegalArgumentException("initial must be positive: " + initial);
		}
		if (max.compareTo(initial) < 0) {
			throw new IllegalArgumentException("max must not be shorter than initial: " + max);
		}
	}

	public Duration delayAfter(final int failedAttempts) {
		if (failedAttempts < 1) {
			throw new IllegalArgumentException("failedAttempts must be positive: " + failedAttempts);
		}

		final int shift = Math.min(failedAttempts - 1, MAX_SHIFT);
		final long multiplier = 1L << shift;
		final long initialMillis = initial.toMillis();

		if (initialMillis > max.toMillis() / multiplier) {
			return max;
		}
		return Duration.ofMillis(initialMillis * multiplier);
	}
}
