package org.example.packing.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.packing.acceptance")
public record PackageAcceptanceProperties(
		@NotNull @Positive Double maxLengthCm,
		@NotNull @Positive Double maxWidthCm,
		@NotNull @Positive Double maxHeightCm,
		@NotNull @Positive Double maxWeightKg
) {
}
