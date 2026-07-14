package org.example.packing.infrastructure.kafka.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PackageCreateRequestEvent(
		@NotNull UUID eventId,

		@NotBlank String eventType,

		@Positive int version,

		@NotNull Instant occurredAt,

		@NotEmpty
		@Valid
		List<PackageCreateRequestItem> packages
) {
}