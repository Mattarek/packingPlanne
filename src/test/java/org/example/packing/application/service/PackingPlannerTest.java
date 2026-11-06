package org.example.packing.application.service;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.domain.exception.NoVehiclesAvailableException;
import org.example.packing.domain.exception.PackageTooLargeException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.example.packing.domain.packing.ExtremePointPackingStrategy;
import org.example.packing.domain.packing.PackageOrdering;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackingServiceTest {

	private final Vehicle van = new Vehicle(
			UUID.randomUUID(), "Van", new Dimensions(100.0, 100.0, 100.0), new Weight(100.0)
	);

	@Test
	void shouldMeasureElapsedTimeWithTheInjectedClock() {
		// given
		final Iterator<Long> readings = List.of(1_000_000L, 8_000_000L).iterator();
		final PackingPlanner service = new PackingPlanner(
				new ExtremePointPackingStrategy(PackageOrdering.VOLUME_DESC),
				readings::next
		);

		// when
		final PackingReport report = service.pack(List.of(pkg(10.0, 1.0)), List.of(van));

		// then
		assertThat(report.elapsedMillis()).isEqualTo(7L);
	}

	@Test
	void shouldBuildReportWithStatistics() {
		// when
		final PackingReport report = service().pack(List.of(pkg(10.0, 1.0), pkg(10.0, 1.0)), List.of(van));

		// then
		assertThat(report.strategyName()).isEqualTo("ExtremePoint(VOLUME_DESC)");
		assertThat(report.totalPackagesIn()).isEqualTo(2);
		assertThat(report.packedVehicles()).hasSize(1);
		assertThat(report.statistics().packedPackagesCount()).isEqualTo(2);
		assertThat(report.statistics().fullyPacked()).isTrue();
	}

	@Test
	void shouldRejectRunWithoutVehiclesAsBusinessRuleViolation() {
		// when & then
		assertThatThrownBy(() -> service().pack(List.of(pkg(10.0, 1.0)), List.of()))
				.isInstanceOf(NoVehiclesAvailableException.class);
	}

	@Test
	void shouldRejectPackageThatFitsNoVehicle() {
		// given
		final Package tooBig = pkg(200.0, 1.0);

		// when & then
		assertThatThrownBy(() -> service().pack(List.of(tooBig), List.of(van)))
				.isInstanceOf(PackageTooLargeException.class)
				.hasMessageContaining(tooBig.id().toString())
				.hasMessageNotContaining(van.id().toString());
	}

	private static PackingPlanner service() {
		return new PackingPlanner(new ExtremePointPackingStrategy(PackageOrdering.VOLUME_DESC), () -> 0L);
	}

	private static Package pkg(final double edge, final double kilograms) {
		return new Package(UUID.randomUUID(), new Dimensions(edge, edge, edge), new Weight(kilograms));
	}
}
