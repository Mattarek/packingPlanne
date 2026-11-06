package org.example.packing.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import org.example.packing.domain.packing.PackageOrdering;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.packing.strategy")
public record PackingStrategyProperties(
		@NotNull PackageOrdering ordering
) {
}
