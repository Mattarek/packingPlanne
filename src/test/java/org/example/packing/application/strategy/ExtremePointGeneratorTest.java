package org.example.packing.application.strategy;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicle;
import org.example.packing.domain.model.PlacedPackage;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExtremePointPackingStrategyOrderingTest {

	private Package largestVolumePackage;
	private Package heaviestPackage;
	private Package tallestPackage;
	private Package largestBaseAreaPackage;

	private Vehicle vehicle;

	@BeforeAll
	void setUp() {
		largestVolumePackage = createPackage(
				new Dimensions(30.0, 30.0, 30.0),
				new Weight(10.0)
		);

		heaviestPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(100.0)
		);

		tallestPackage = createPackage(
				new Dimensions(5.0, 5.0, 80.0),
				new Weight(5.0)
		);

		largestBaseAreaPackage = createPackage(
				new Dimensions(60.0, 60.0, 1.0),
				new Weight(10.0)
		);

		vehicle = createVehicle();
	}

	@Test
	void shouldPackLargestVolumePackageFirstWhenUsingVolumeDesc() {
		// given
		final ExtremePointPackingStrategy strategy =
				new ExtremePointPackingStrategy(PackageOrdering.VOLUME_DESC);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(heaviestPackage, tallestPackage, largestVolumePackage),
				List.of(vehicle)
		);

		// then
		assertThat(firstPackedPackage(result)).isEqualTo(largestVolumePackage);
	}

	@Test
	void shouldPackHeaviestPackageFirstWhenUsingWeightDesc() {
		// given
		final ExtremePointPackingStrategy strategy =
				new ExtremePointPackingStrategy(PackageOrdering.WEIGHT_DESC);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(largestVolumePackage, tallestPackage, heaviestPackage),
				List.of(createVehicle())
		);

		// then
		assertThat(firstPackedPackage(result)).isEqualTo(heaviestPackage);
	}

	@Test
	void shouldPackLargestBaseAreaPackageFirstWhenUsingBaseAreaDesc() {
		// given
		final ExtremePointPackingStrategy strategy =
				new ExtremePointPackingStrategy(PackageOrdering.BASE_AREA_DESC);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(largestVolumePackage, tallestPackage, largestBaseAreaPackage),
				List.of(createVehicle())
		);

		// then
		assertThat(firstPackedPackage(result)).isEqualTo(largestBaseAreaPackage);
	}

	@Test
	void shouldPackTallestPackageFirstWhenUsingHeightDesc() {
		// given
		final ExtremePointPackingStrategy strategy =
				new ExtremePointPackingStrategy(PackageOrdering.HEIGHT_DESC);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(largestVolumePackage, heaviestPackage, tallestPackage),
				List.of(createVehicle())
		);

		// then
		assertThat(firstPackedPackage(result)).isEqualTo(tallestPackage);
	}

	private Package firstPackedPackage(final PackingStrategy.PackingResult result) {
		final PackedVehicle packedVehicle = result.packedVehicles().get(0);
		final PlacedPackage placedPackage = packedVehicle.placedPackages().get(0);
		return placedPackage.pkg();
	}

	private Vehicle createVehicle() {
		return new Vehicle(
				UUID.randomUUID(),
				"Test vehicle",
				new Dimensions(200.0, 200.0, 200.0),
				new Weight(1000.0)
		);
	}

	private Package createPackage(final Dimensions dimensions, final Weight weight) {
		return new Package(
				UUID.randomUUID(),
				dimensions,
				weight
		);
	}
}