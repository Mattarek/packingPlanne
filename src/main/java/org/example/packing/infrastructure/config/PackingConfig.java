package org.example.packing.infrastructure.config;

import org.example.packing.application.port.out.MonotonicClock;
import org.example.packing.application.service.PackingPlanner;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Weight;
import org.example.packing.domain.packing.ExtremePointGenerator;
import org.example.packing.domain.packing.ExtremePointPackingStrategy;
import org.example.packing.domain.packing.PackingStrategy;
import org.example.packing.domain.packing.PositionPreferences;
import org.example.packing.domain.policy.MaxDimensionsRule;
import org.example.packing.domain.policy.MaxWeightRule;
import org.example.packing.domain.policy.PackageAcceptancePolicy;
import org.example.packing.domain.policy.TransportableCategoryRule;
import org.example.packing.domain.policy.UltraFragileRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PackingConfig {

	@Bean
	public PackingStrategy packingStrategy(final PackingStrategyProperties properties) {
		return new ExtremePointPackingStrategy(
				properties.ordering(),
				PositionPreferences.BOTTOM_LEFT_BACK,
				new ExtremePointGenerator()
		);
	}

	@Bean
	public PackingPlanner packingPlanner(
			final PackingStrategy packingStrategy,
			final MonotonicClock monotonicClock
	) {
		return new PackingPlanner(packingStrategy, monotonicClock);
	}

	@Bean
	public PackageAcceptancePolicy packageAcceptancePolicy(final PackageAcceptanceProperties limits) {
		return new PackageAcceptancePolicy(List.of(
				new MaxDimensionsRule(new Dimensions(
						limits.maxLengthCm(),
						limits.maxWidthCm(),
						limits.maxHeightCm()
				)),
				new MaxWeightRule(new Weight(limits.maxWeightKg())),
				new TransportableCategoryRule(),
				new UltraFragileRule()
		));
	}
}
