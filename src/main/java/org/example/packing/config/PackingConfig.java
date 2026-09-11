package org.example.packing.config;

import org.example.packing.application.service.PackingService;
import org.example.packing.application.service.ReportPrinter;
import org.example.packing.application.strategy.ExtremePointPackingStrategy;
import org.example.packing.application.strategy.PackageOrdering;
import org.example.packing.application.strategy.PackingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PackingConfig {

	@Bean
	public PackingStrategy packingStrategy() {
		return new ExtremePointPackingStrategy(PackageOrdering.BASE_AREA_DESC);
	}

	@Bean
	public PackingService packingService(final PackingStrategy packingStrategy) {
		return new PackingService(packingStrategy);
	}

	@Bean
	public ReportPrinter reportPrinter() {
		return new ReportPrinter();
	}
}
