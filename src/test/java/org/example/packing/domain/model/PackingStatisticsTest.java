package org.example.packing.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PackingStatisticsTest {

	private final Vehicle vehicle = new Vehicle(
			UUID.randomUUID(), "Van", new Dimensions(100.0, 100.0, 100.0), new Weight(100.0)
	);

	@Test
	void shouldIgnoreUnusedVehiclesInCountsAndAverages() {
		// given
		final PackedVehicle used = new PackedVehicle(vehicle);
		used.place(pkg(50.0, 10.0), Position.ORIGIN);
		final PackedVehicle empty = new PackedVehicle(vehicle);

		// when
		final PackingStatistics stats = PackingStatistics.of(List.of(used.snapshot(), empty.snapshot()), List.of());

		// then
		assertThat(stats.packedPackagesCount()).isEqualTo(1);
		assertThat(stats.vehiclesUsed()).isEqualTo(1);
		assertThat(stats.fullyPacked()).isTrue();
		assertThat(stats.averageVolumeUtilization()).isEqualTo(0.125);
		assertThat(stats.averageWeightUtilization()).isEqualTo(0.1);
	}

	@Test
	void shouldNotBeFullyPackedWhenSomethingIsLeftOver() {
		// when
		final PackingStatistics stats = PackingStatistics.of(List.of(), List.of(pkg(10.0, 1.0)));

		// then
		assertThat(stats.fullyPacked()).isFalse();
		assertThat(stats.vehiclesUsed()).isZero();
		assertThat(stats.averageVolumeUtilization()).isZero();
		assertThat(stats.averageWeightUtilization()).isZero();
	}

	private static Package pkg(final double edge, final double kilograms) {
		return new Package(UUID.randomUUID(), new Dimensions(edge, edge, edge), new Weight(kilograms));
	}

	@Test
	void snapshotShouldNotChangeWhenTheVehicleIsPackedFurther() {
		// given
		final PackedVehicle packed = new PackedVehicle(vehicle);
		packed.place(pkg(10.0, 1.0), Position.ORIGIN);
		final PackedVehicleSnapshot snapshot = packed.snapshot();

		// when
		packed.place(pkg(10.0, 1.0), new Position(10.0, 0.0, 0.0));

		// then
		assertThat(snapshot.placedPackages()).hasSize(1);
		assertThat(snapshot.currentWeight()).isEqualTo(new Weight(1.0));
		assertThat(packed.placedPackages()).hasSize(2);
	}
}
