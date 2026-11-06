package org.example.packing.infrastructure.messaging.deadletter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka.dead-letter")
public record DeadLetterProperties(
		@NotNull @Positive Integer batchSize,
		@NotNull @Positive Integer maxAttempts
) {
}
