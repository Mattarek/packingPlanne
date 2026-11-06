package org.example.packing.infrastructure.messaging.inbox;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.kafka.inbox")
public record InboxProperties(
		@NotNull @Positive Integer batchSize,
		@NotNull @Positive Integer workerThreads,
		@NotNull Duration lease,
		@NotNull @Positive Integer maxAttempts,
		@NotNull Duration initialBackoff,
		@NotNull Duration maxBackoff,
		@NotNull Duration ledgerRetention,
		@NotNull @Positive Integer cleanupBatchSize
) {
}
