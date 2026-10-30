package org.example.packing.infrastructure.messaging.outbox;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.kafka.outbox")
public record OutboxProperties(
		@NotNull @Positive Integer batchSize,
		@NotNull @Positive Integer maxAttempts,
		@NotNull Duration sendTimeout,
		@NotNull Duration retention
) {
}
