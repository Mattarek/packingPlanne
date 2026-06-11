package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.dto.PackedVehicleResponse;
import org.example.packing.application.dto.PlacedPackageResponse;
import org.example.packing.application.dto.PositionResponse;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicle;
import org.example.packing.domain.model.PlacedPackage;
import org.example.packing.domain.model.Position;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PackingReportResponseMapperTest {

	private PackingReportResponseMapper mapper;

	private UUID vehicleId;
	private UUID packageId;

	private Vehicle vehicle;
	private Package pkg;
	private Position position;
	private PlacedPackage placedPackage;
	private PackedVehicle packedVehicle;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(PackingReportResponseMapper.class);

		vehicleId = UUID.randomUUID();
		packageId = UUID.randomUUID();

		vehicle = new Vehicle(
				vehicleId,
				"Test vehicle",
				new Dimensions(100.0, 50.0, 40.0),
				new Weight(1000.0)
		);

		pkg = new Package(
				packageId,
				new Dimensions(10.0, 20.0, 30.0),
				new Weight(100.0)
		);

		position = new Position(1.0, 2.0, 3.0);

		placedPackage = new PlacedPackage(pkg, position);

		packedVehicle = new PackedVehicle(vehicle);
		packedVehicle.place(pkg, position);
	}

	@Test
	void shouldMapVehicleToVehicleResponse() {
		// when
		final VehicleResponse result = mapper.toResponse(vehicle);

		// then
		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(vehicleId);
		assertThat(result.name()).isEqualTo("Test vehicle");
		assertThat(result.length()).isEqualTo(100.0);
		assertThat(result.width()).isEqualTo(50.0);
		assertThat(result.height()).isEqualTo(40.0);
		assertThat(result.maxPayload()).isEqualTo(1000.0);
	}

	@Test
	void shouldMapPackageToPackageResponse() {
		// when
		final PackageResponse result = mapper.toResponse(pkg);

		// then
		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(packageId);
		assertThat(result.length()).isEqualTo(10.0);
		assertThat(result.width()).isEqualTo(20.0);
		assertThat(result.height()).isEqualTo(30.0);
		assertThat(result.weight()).isEqualTo(100.0);
	}

	@Test
	void shouldMapPositionToPositionResponse() {
		// when
		final PositionResponse result = mapper.toResponse(position);

		// then
		assertThat(result).isNotNull();
		assertThat(result.x()).isEqualTo(1.0);
		assertThat(result.y()).isEqualTo(2.0);
		assertThat(result.z()).isEqualTo(3.0);
	}

	@Test
	void shouldMapPlacedPackageToPlacedPackageResponse() {
		// when
		final PlacedPackageResponse result = mapper.toResponse(placedPackage);

		// then
		assertThat(result).isNotNull();

		assertThat(result.pkg()).isNotNull();
		assertThat(result.pkg().id()).isEqualTo(packageId);
		assertThat(result.pkg().length()).isEqualTo(10.0);
		assertThat(result.pkg().width()).isEqualTo(20.0);
		assertThat(result.pkg().height()).isEqualTo(30.0);
		assertThat(result.pkg().weight()).isEqualTo(100.0);

		assertThat(result.position()).isNotNull();
		assertThat(result.position().x()).isEqualTo(1.0);
		assertThat(result.position().y()).isEqualTo(2.0);
		assertThat(result.position().z()).isEqualTo(3.0);
	}

	@Test
	void shouldMapPackedVehicleToPackedVehicleResponse() {
		// when
		final PackedVehicleResponse result = mapper.toResponse(packedVehicle);

		// then
		assertThat(result).isNotNull();

		assertThat(result.vehicle()).isNotNull();
		assertThat(result.vehicle().id()).isEqualTo(vehicleId);
		assertThat(result.vehicle().name()).isEqualTo("Test vehicle");
		assertThat(result.vehicle().length()).isEqualTo(100.0);
		assertThat(result.vehicle().width()).isEqualTo(50.0);
		assertThat(result.vehicle().height()).isEqualTo(40.0);
		assertThat(result.vehicle().maxPayload()).isEqualTo(1000.0);

		assertThat(result.placedPackages()).hasSize(1);

		final PlacedPackageResponse placedPackageResponse = result.placedPackages().get(0);

		assertThat(placedPackageResponse.pkg().id()).isEqualTo(packageId);
		assertThat(placedPackageResponse.position().x()).isEqualTo(1.0);
		assertThat(placedPackageResponse.position().y()).isEqualTo(2.0);
		assertThat(placedPackageResponse.position().z()).isEqualTo(3.0);

		assertThat(result.currentWeightKg()).isEqualTo(100.0);
		assertThat(result.currentVolume()).isEqualTo(6000.0);
		assertThat(result.remainingPayloadKg()).isEqualTo(900.0);

		assertThat(result.volumeUtilization()).isEqualTo(0.03);
		assertThat(result.weightUtilization()).isEqualTo(0.1);
	}

	@Test
	void shouldMapWeightToDouble() {
		// given
		final Weight weight = new Weight(123.45);

		// when
		final double result = mapper.map(weight);

		// then
		assertThat(result).isEqualTo(123.45);
	}
}