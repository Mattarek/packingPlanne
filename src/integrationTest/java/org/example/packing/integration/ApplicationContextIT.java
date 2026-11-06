package org.example.packing.integration;

import org.example.packing.application.port.in.PackageUseCase;
import org.example.packing.application.port.in.VehicleUseCase;
import org.example.packing.domain.packing.PackingStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationContextIT extends AbstractPostgresIntegrationTest {

	private final PackageUseCase packageUseCase;
	private final VehicleUseCase vehicleUseCase;
	private final PackingStrategy packingStrategy;

	public ApplicationContextIT(final PackageUseCase packageUseCase, final VehicleUseCase vehicleUseCase, final PackingStrategy packingStrategy) {
		this.packageUseCase = packageUseCase;
		this.vehicleUseCase = vehicleUseCase;
		this.packingStrategy = packingStrategy;
	}

	@Test
	void shouldLoadApplicationContextWithPostgresContainer() {
		assertThat(packageUseCase).isNotNull();
		assertThat(vehicleUseCase).isNotNull();
		assertThat(packingStrategy).isNotNull();
	}

	@Test
	void shouldUseConfiguredPackingStrategy() {
		assertThat(packingStrategy.name()).isEqualTo("ExtremePoint(BASE_AREA_DESC)");
	}
}