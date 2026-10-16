package org.example.packing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.packing.acceptance")
public record PackageAcceptanceProperties(
		@DefaultValue("120.0") double maxLengthCm,
		@DefaultValue("80.0") double maxWidthCm,
		@DefaultValue("80.0") double maxHeightCm,
		@DefaultValue("30.0") double maxWeightKg
) {
}
