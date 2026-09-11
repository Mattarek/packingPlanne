package org.example.packing.integration;

import org.example.packing.application.service.PackageService;
import org.example.packing.application.service.VehicleService;
import org.example.packing.application.strategy.PackingStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationContextIT extends AbstractPostgresIntegrationTest {

	private final PackageService packageService;
	private final VehicleService vehicleService;
	private final PackingStrategy packingStrategy;

	public ApplicationContextIT(final PackageService packageService, final VehicleService vehicleService, final PackingStrategy packingStrategy) {
		this.packageService = packageService;
		this.vehicleService = vehicleService;
		this.packingStrategy = packingStrategy;
	}

	@Test
	void shouldLoadApplicationContextWithPostgresContainer() {
		assertThat(packageService).isNotNull();
		assertThat(vehicleService).isNotNull();
		assertThat(packingStrategy).isNotNull();
	}

	@Test
	void shouldUseConfiguredPackingStrategy() {
		assertThat(packingStrategy.name()).isEqualTo("ExtremePoint(BASE_AREA_DESC)");
	}
}