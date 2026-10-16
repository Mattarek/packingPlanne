package org.example.packing.config;

import org.example.packing.application.service.PackingService;
import org.example.packing.application.service.ReportPrinter;
import org.example.packing.application.strategy.BottomLeftBackPositionScorer;
import org.example.packing.application.strategy.ExtremePointPackingStrategy;
import org.example.packing.application.strategy.PackageOrdering;
import org.example.packing.application.strategy.PackingStrategy;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Weight;
import org.example.packing.domain.policy.MaxDimensionsRule;
import org.example.packing.domain.policy.MaxWeightRule;
import org.example.packing.domain.policy.PackageAcceptancePolicy;
import org.example.packing.domain.policy.TransportableCategoryRule;
import org.example.packing.domain.policy.UltraFragileRule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Composition root of the packing domain: the one place that decides which
 * concrete strategies and rules are used. Adding an acceptance rule is a new
 * class plus one line here – nothing else changes.
 */
@Configuration
@EnableConfigurationProperties(PackageAcceptanceProperties.class)
public class PackingConfig {

	@Bean
	public PackingStrategy packingStrategy() {
		return new ExtremePointPackingStrategy(
				PackageOrdering.BASE_AREA_DESC,
				new BottomLeftBackPositionScorer()
		);
	}

	@Bean
	public PackingService packingService(final PackingStrategy packingStrategy) {
		return new PackingService(packingStrategy);
	}

	@Bean
	public ReportPrinter reportPrinter() {
		return new ReportPrinter();
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
