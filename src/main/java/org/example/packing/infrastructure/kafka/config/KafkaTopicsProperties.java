package org.example.packing.infrastructure.kafka.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
		@NotNull @Positive Integer partitions,
		@NotBlank String packageCreateRequests,
		@NotBlank String packageCreatedEvents
) {
}
