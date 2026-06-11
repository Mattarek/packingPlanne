package org.example.packing.application.strategy;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicle;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackingResultTest {

	private Vehicle vehicle;
	private PackedVehicle packedVehicle;
	private Package pkg;

	@BeforeEach
	void setUp() {
		vehicle = new Vehicle(
				UUID.randomUUID(),
				"Test vehicle",
				new Dimensions(100.0, 100.0, 100.0),
				new Weight(1000.0)
		);

		packedVehicle = new PackedVehicle(vehicle);

		pkg = new Package(
				UUID.randomUUID(),
				new Dimensions(10.0, 10.0, 10.0),
				new Weight(10.0)
		);
	}

	@Test
	void shouldBeFullyPackedWhenUnpackedPackagesListIsEmpty() {
		// given
		final PackingStrategy.PackingResult result = new PackingStrategy.PackingResult(
				List.of(packedVehicle),
				List.of()
		);

		// when & then
		assertThat(result.isFullyPacked()).isTrue();
	}

	@Test
	void shouldNotBeFullyPackedWhenUnpackedPackagesListIsNotEmpty() {
		// given
		final PackingStrategy.PackingResult result = new PackingStrategy.PackingResult(
				List.of(packedVehicle),
				List.of(pkg)
		);

		// when & then
		assertThat(result.isFullyPacked()).isFalse();
	}

	@Test
	void shouldCopyInputLists() {
		// given
		final List<PackedVehicle> packedVehicles = new ArrayList<>();
		final List<Package> unpackedPackages = new ArrayList<>();

		packedVehicles.add(packedVehicle);
		unpackedPackages.add(pkg);

		final PackingStrategy.PackingResult result = new PackingStrategy.PackingResult(
				packedVehicles,
				unpackedPackages
		);

		// when
		packedVehicles.clear();
		unpackedPackages.clear();

		// then
		assertThat(result.packedVehicles()).containsExactly(packedVehicle);
		assertThat(result.unpackedPackages()).containsExactly(pkg);
	}

	@Test
	void shouldExposeUnmodifiableLists() {
		// given
		final PackingStrategy.PackingResult result = new PackingStrategy.PackingResult(
				List.of(packedVehicle),
				List.of(pkg)
		);

		// when & then
		assertThatThrownBy(() -> result.packedVehicles().add(packedVehicle))
				.isInstanceOf(UnsupportedOperationException.class);

		assertThatThrownBy(() -> result.unpackedPackages().add(pkg))
				.isInstanceOf(UnsupportedOperationException.class);
	}
}