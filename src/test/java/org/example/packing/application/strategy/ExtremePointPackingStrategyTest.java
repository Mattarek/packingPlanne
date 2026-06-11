package org.example.packing.application.strategy;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicle;
import org.example.packing.domain.model.PlacedPackage;
import org.example.packing.domain.model.Position;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtremePointPackingStrategyTest {

	private ExtremePointPackingStrategy strategy;

	private Vehicle vehicle;

	@BeforeEach
	void setUp() {
		strategy = new ExtremePointPackingStrategy(PackageOrdering.BASE_AREA_DESC);

		vehicle = createVehicle(
				new Dimensions(100.0, 100.0, 100.0),
				new Weight(1000.0)
		);
	}

	@Test
	void shouldCreateDefaultStrategyWithVolumeDescendingOrdering() {
		// when
		final ExtremePointPackingStrategy result = new ExtremePointPackingStrategy();

		// then
		assertThat(result.name()).isEqualTo("ExtremePoint(VOLUME_DESC)");
		assertThat(result.ordering()).isEqualTo(PackageOrdering.VOLUME_DESC);
	}

	@Test
	void shouldThrowExceptionWhenOrderingIsNull() {
		// when & then
		assertThatThrownBy(() -> new ExtremePointPackingStrategy(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ordering must not be null");
	}

	@Test
	void shouldReturnStrategyNameWithOrderingName() {
		// when
		final String result = strategy.name();

		// then
		assertThat(result).isEqualTo("ExtremePoint(BASE_AREA_DESC)");
	}

	@Test
	void shouldThrowExceptionWhenPackagesListIsNull() {
		// when & then
		assertThatThrownBy(() -> strategy.pack(null, List.of(vehicle)))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("packages must not be null");
	}

	@Test
	void shouldThrowExceptionWhenVehiclesListIsNull() {
		// given
		final Package pkg = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(10.0)
		);

		// when & then
		assertThatThrownBy(() -> strategy.pack(List.of(pkg), null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("vehicles must not be null");
	}

	@Test
	void shouldReturnFullyPackedResultWhenAllPackagesFit() {
		// given
		final Package first = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(100.0)
		);

		final Package second = createPackage(
				new Dimensions(20.0, 10.0, 10.0),
				new Weight(200.0)
		);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(first, second),
				List.of(vehicle)
		);

		// then
		assertThat(result.isFullyPacked()).isTrue();
		assertThat(result.unpackedPackages()).isEmpty();
		assertThat(result.packedVehicles()).hasSize(1);

		final PackedVehicle packedVehicle = result.packedVehicles().get(0);

		assertThat(packedVehicle.placedPackages()).hasSize(2);
		assertThat(packedVehicle.currentWeight()).isEqualTo(new Weight(300.0));
	}

	@Test
	void shouldPutPackageIntoUnpackedWhenThereIsNoVehicle() {
		// given
		final Package pkg = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(100.0)
		);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(pkg),
				List.of()
		);

		// then
		assertThat(result.isFullyPacked()).isFalse();
		assertThat(result.packedVehicles()).isEmpty();
		assertThat(result.unpackedPackages()).containsExactly(pkg);
	}

	@Test
	void shouldPutPackageIntoUnpackedWhenItExceedsVehiclePayload() {
		// given
		final Package tooHeavyPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(1001.0)
		);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(tooHeavyPackage),
				List.of(vehicle)
		);

		// then
		assertThat(result.isFullyPacked()).isFalse();
		assertThat(result.unpackedPackages()).containsExactly(tooHeavyPackage);
	}

	@Test
	void shouldPutPackageIntoUnpackedWhenItDoesNotFitInsideCargoArea() {
		// given
		final Package tooLargePackage = createPackage(
				new Dimensions(101.0, 10.0, 10.0),
				new Weight(100.0)
		);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(tooLargePackage),
				List.of(vehicle)
		);

		// then
		assertThat(result.isFullyPacked()).isFalse();
		assertThat(result.unpackedPackages()).containsExactly(tooLargePackage);
	}

	@Test
	void shouldUseMultipleVehiclesWhenOneVehicleIsNotEnough() {
		// given
		final Vehicle firstVehicle = createVehicle(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(100.0)
		);

		final Vehicle secondVehicle = createVehicle(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(100.0)
		);

		final Package firstPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(100.0)
		);

		final Package secondPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(100.0)
		);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(firstPackage, secondPackage),
				List.of(firstVehicle, secondVehicle)
		);

		// then
		assertThat(result.isFullyPacked()).isTrue();
		assertThat(result.unpackedPackages()).isEmpty();
		assertThat(result.packedVehicles()).hasSize(2);

		assertThat(result.packedVehicles().get(0).placedPackages()).hasSize(1);
		assertThat(result.packedVehicles().get(1).placedPackages()).hasSize(1);
	}

	@Test
	void shouldNotMutateInputPackagesList() {
		// given
		final Package smallPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(10.0)
		);

		final Package largeBasePackage = createPackage(
				new Dimensions(50.0, 50.0, 1.0),
				new Weight(10.0)
		);

		final List<Package> packages = new ArrayList<>(
				List.of(smallPackage, largeBasePackage)
		);

		// when
		strategy.pack(packages, List.of(vehicle));

		// then
		assertThat(packages).containsExactly(smallPackage, largeBasePackage);
	}

	@Test
	void shouldPackPackagesUsingSelectedOrdering() {
		// given
		final Package smallBasePackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(10.0)
		);

		final Package largeBasePackage = createPackage(
				new Dimensions(50.0, 50.0, 1.0),
				new Weight(10.0)
		);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(smallBasePackage, largeBasePackage),
				List.of(vehicle)
		);

		// then
		final List<PlacedPackage> placedPackages = result.packedVehicles()
				.get(0)
				.placedPackages();

		assertThat(placedPackages.get(0).pkg()).isEqualTo(largeBasePackage);
		assertThat(placedPackages.get(1).pkg()).isEqualTo(smallBasePackage);
	}

	@Test
	void shouldPlaceFirstPackageAtOrigin() {
		// given
		final Package pkg = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(100.0)
		);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(pkg),
				List.of(vehicle)
		);

		// then
		final PlacedPackage placedPackage = result.packedVehicles()
				.get(0)
				.placedPackages()
				.get(0);

		assertThat(placedPackage.position()).isEqualTo(Position.ORIGIN);
	}

	@Test
	void shouldNotCreateOverlappingPlacements() {
		// given
		final Package first = createPackage(
				new Dimensions(50.0, 50.0, 50.0),
				new Weight(100.0)
		);

		final Package second = createPackage(
				new Dimensions(50.0, 50.0, 50.0),
				new Weight(100.0)
		);

		final Package third = createPackage(
				new Dimensions(50.0, 50.0, 50.0),
				new Weight(100.0)
		);

		// when
		final PackingStrategy.PackingResult result = strategy.pack(
				List.of(first, second, third),
				List.of(vehicle)
		);

		// then
		final List<PlacedPackage> placedPackages = result.packedVehicles()
				.get(0)
				.placedPackages();

		for (int i = 0; i < placedPackages.size(); i++) {
			for (int j = i + 1; j < placedPackages.size(); j++) {
				assertThat(placedPackages.get(i).overlapsWith(placedPackages.get(j)))
						.isFalse();
			}
		}
	}

	private Vehicle createVehicle(final Dimensions cargoArea, final Weight maxPayload) {
		return new Vehicle(
				UUID.randomUUID(),
				"Test vehicle",
				cargoArea,
				maxPayload
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