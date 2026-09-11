package org.example.packing.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackedVehicleTest {

	private Vehicle vehicle;
	private PackedVehicle packedVehicle;

	private Dimensions cargoArea;
	private Weight maxPayload;

	private Package firstPackage;
	private Package secondPackage;

	private Position originPosition;
	private Position nonOverlappingPosition;
	private Position overlappingPosition;
	private Position outsideCargoAreaPosition;

	@BeforeEach
	void setUp() {
		cargoArea = new Dimensions(100.0, 100.0, 100.0);
		maxPayload = new Weight(1000.0);

		vehicle = createVehicle(cargoArea, maxPayload);
		packedVehicle = new PackedVehicle(vehicle);

		firstPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(100.0)
		);

		secondPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(200.0)
		);

		originPosition = new Position(0.0, 0.0, 0.0);
		nonOverlappingPosition = new Position(10.0, 0.0, 0.0);
		overlappingPosition = new Position(5.0, 0.0, 0.0);
		outsideCargoAreaPosition = new Position(95.0, 0.0, 0.0);
	}

	@Test
	void shouldCreatePackedVehicleWhenVehicleIsNotNull() {
		// when
		final PackedVehicle result = new PackedVehicle(vehicle);

		// then
		assertThat(result.vehicle()).isEqualTo(vehicle);
		assertThat(result.placedPackages()).isEmpty();
		assertThat(result.currentWeight()).isEqualTo(Weight.ZERO);
	}

	@Test
	void shouldThrowExceptionWhenVehicleIsNull() {
		// when & then
		assertThatThrownBy(() -> new PackedVehicle(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("vehicle must not be null");
	}

	@Test
	void shouldReturnUnmodifiablePlacedPackagesList() {
		// when & then
		assertThatThrownBy(() -> packedVehicle.placedPackages().add(
				new PlacedPackage(firstPackage, originPosition)
		)).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void shouldReturnInitialRemainingPayloadEqualToVehicleMaxPayload() {
		// when
		final Weight result = packedVehicle.remainingPayload();

		// then
		assertThat(result).isEqualTo(maxPayload);
	}

	@Test
	void shouldReturnZeroCurrentVolumeWhenNoPackagesArePlaced() {
		// when
		final double result = packedVehicle.currentVolume();

		// then
		assertThat(result).isEqualTo(0.0);
	}

	@Test
	void shouldReturnZeroVolumeUtilizationWhenNoPackagesArePlaced() {
		// when
		final double result = packedVehicle.volumeUtilization();

		// then
		assertThat(result).isEqualTo(0.0);
	}

	@Test
	void shouldReturnZeroWeightUtilizationWhenNoPackagesArePlaced() {
		// when
		final double result = packedVehicle.weightUtilization();

		// then
		assertThat(result).isEqualTo(0.0);
	}

	@Test
	void shouldReturnTrueWhenPackageCanBePlaced() {
		// when
		final boolean result = packedVehicle.canPlace(firstPackage, originPosition);

		// then
		assertThat(result).isTrue();
	}

	@Test
	void shouldThrowExceptionWhenCheckingPlacementForNullPackage() {
		// when & then
		assertThatThrownBy(() -> packedVehicle.canPlace(null, originPosition))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("pkg must not be null");
	}

	@Test
	void shouldThrowExceptionWhenCheckingPlacementForNullPosition() {
		// when & then
		assertThatThrownBy(() -> packedVehicle.canPlace(firstPackage, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("position must not be null");
	}

	@Test
	void shouldReturnFalseWhenPackageWouldExceedMaxPayload() {
		// given
		final Package tooHeavyPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(1001.0)
		);

		// when
		final boolean result = packedVehicle.canPlace(tooHeavyPackage, originPosition);

		// then
		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnFalseWhenPackageWouldBeOutsideCargoArea() {
		// when
		final boolean result = packedVehicle.canPlace(firstPackage, outsideCargoAreaPosition);

		// then
		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnFalseWhenPackageWouldOverlapWithAlreadyPlacedPackage() {
		// given
		packedVehicle.place(firstPackage, originPosition);

		// when
		final boolean result = packedVehicle.canPlace(secondPackage, overlappingPosition);

		// then
		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnTrueWhenPackageDoesNotOverlapWithAlreadyPlacedPackage() {
		// given
		packedVehicle.place(firstPackage, originPosition);

		// when
		final boolean result = packedVehicle.canPlace(secondPackage, nonOverlappingPosition);

		// then
		assertThat(result).isTrue();
	}

	@Test
	void shouldNotMutateStateWhenCanPlaceIsCalled() {
		// when
		final boolean result = packedVehicle.canPlace(firstPackage, originPosition);

		// then
		assertThat(result).isTrue();
		assertThat(packedVehicle.placedPackages()).isEmpty();
		assertThat(packedVehicle.currentWeight()).isEqualTo(Weight.ZERO);
	}

	@Test
	void shouldPlacePackageWhenPlacementIsValid() {
		// when
		packedVehicle.place(firstPackage, originPosition);

		// then
		assertThat(packedVehicle.placedPackages()).hasSize(1);
		assertThat(packedVehicle.placedPackages().getFirst().pkg()).isEqualTo(firstPackage);
		assertThat(packedVehicle.placedPackages().getFirst().position()).isEqualTo(originPosition);
		assertThat(packedVehicle.currentWeight()).isEqualTo(new Weight(100.0));
	}

	@Test
	void shouldUpdateCurrentWeightAfterPlacingMultiplePackages() {
		// when
		packedVehicle.place(firstPackage, originPosition);
		packedVehicle.place(secondPackage, nonOverlappingPosition);

		// then
		assertThat(packedVehicle.currentWeight()).isEqualTo(new Weight(300.0));
	}

	@Test
	void shouldUpdateRemainingPayloadAfterPlacingPackage() {
		// given
		packedVehicle.place(firstPackage, originPosition);

		// when
		final Weight result = packedVehicle.remainingPayload();

		// then
		assertThat(result).isEqualTo(new Weight(900.0));
	}

	@Test
	void shouldUpdateCurrentVolumeAfterPlacingPackage() {
		// given
		packedVehicle.place(firstPackage, originPosition);

		// when
		final double result = packedVehicle.currentVolume();

		// then
		assertThat(result).isEqualTo(1000.0);
	}

	@Test
	void shouldCalculateVolumeUtilization() {
		// given
		packedVehicle.place(firstPackage, originPosition);

		// when
		final double result = packedVehicle.volumeUtilization();

		// then
		assertThat(result).isEqualTo(0.001);
	}

	@Test
	void shouldCalculateWeightUtilization() {
		// given
		packedVehicle.place(firstPackage, originPosition);

		// when
		final double result = packedVehicle.weightUtilization();

		// then
		assertThat(result).isEqualTo(0.1);
	}

	@Test
	void shouldThrowExceptionWhenTryingToPlaceInvalidPackage() {
		// given
		final Package tooHeavyPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(1001.0)
		);

		// when & then
		assertThatThrownBy(() -> packedVehicle.place(tooHeavyPackage, originPosition))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Cannot place package")
				.hasMessageContaining("would violate constraints");
	}

	@Test
	void shouldNotMutateStateWhenPlacingInvalidPackage() {
		// given
		final Package tooHeavyPackage = createPackage(
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(1001.0)
		);

		// when
		assertThatThrownBy(() -> packedVehicle.place(tooHeavyPackage, originPosition))
				.isInstanceOf(IllegalStateException.class);

		// then
		assertThat(packedVehicle.placedPackages()).isEmpty();
		assertThat(packedVehicle.currentWeight()).isEqualTo(Weight.ZERO);
		assertThat(packedVehicle.currentVolume()).isEqualTo(0.0);
	}

	@Test
	void shouldReturnSameValuesFromJavaBeanStyleGetters() {
		// given
		packedVehicle.place(firstPackage, originPosition);

		// when & then
		assertThat(packedVehicle.getVehicle()).isEqualTo(packedVehicle.vehicle());
		assertThat(packedVehicle.getPlacedPackages()).isEqualTo(packedVehicle.placedPackages());
		assertThat(packedVehicle.getCurrentWeight()).isEqualTo(packedVehicle.currentWeight());
		assertThat(packedVehicle.getRemainingPayload()).isEqualTo(packedVehicle.remainingPayload());
		assertThat(packedVehicle.getCurrentVolume()).isEqualTo(packedVehicle.currentVolume());
		assertThat(packedVehicle.getVolumeUtilization()).isEqualTo(packedVehicle.volumeUtilization());
		assertThat(packedVehicle.getWeightUtilization()).isEqualTo(packedVehicle.weightUtilization());
	}

	@Test
	void shouldReturnFormattedString() {
		// given
		packedVehicle.place(firstPackage, originPosition);

		// when
		final String result = packedVehicle.toString();

		// then
		assertThat(result)
				.contains("PackedVehicle[")
				.contains(vehicle.id().toString())
				.contains("packed=1")
				.contains("weight=")
				.contains("vol=");
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