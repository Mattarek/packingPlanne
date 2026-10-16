package org.example.packing.application.controller.mapper;

import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.dto.PackedVehicleResponse;
import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.dto.PackingRunResponse;
import org.example.packing.application.dto.PlacedPackageResponse;
import org.example.packing.application.dto.PositionResponse;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.application.mapper.PackingMapperConfig;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicle;
import org.example.packing.domain.model.PlacedPackage;
import org.example.packing.domain.model.Position;
import org.example.packing.domain.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Domain -> REST responses. Changes only when the response JSON changes.
 */
@Mapper(config = PackingMapperConfig.class)
public interface RestResponseMapper {

	@Mapping(target = "length", source = "dimensions.length")
	@Mapping(target = "width", source = "dimensions.width")
	@Mapping(target = "height", source = "dimensions.height")
	@Mapping(target = "weight", source = "weight.kilograms")
	PackageResponse toResponse(Package pkg);

	List<PackageResponse> toPackageResponses(List<Package> packages);

	@Mapping(target = "length", source = "cargoArea.length")
	@Mapping(target = "width", source = "cargoArea.width")
	@Mapping(target = "height", source = "cargoArea.height")
	@Mapping(target = "maxPayload", source = "maxPayload.kilograms")
	VehicleResponse toResponse(Vehicle vehicle);

	List<VehicleResponse> toVehicleResponses(List<Vehicle> vehicles);

	PositionResponse toResponse(Position position);

	PlacedPackageResponse toResponse(PlacedPackage placedPackage);

	List<PlacedPackageResponse> toPlacedPackageResponses(List<PlacedPackage> placedPackages);

	// PackedVehicle is a regular class with record-style accessors (vehicle(),
	// currentWeight(), ...), which MapStruct does not treat as properties. The
	// mapper adapts to the domain via expressions – not the other way round.
	@Mapping(target = "vehicle", expression = "java(toResponse(packedVehicle.vehicle()))")
	@Mapping(target = "placedPackages",
			expression = "java(toPlacedPackageResponses(packedVehicle.placedPackages()))")
	@Mapping(target = "currentWeightKg", expression = "java(packedVehicle.currentWeight().kilograms())")
	@Mapping(target = "currentVolume", expression = "java(packedVehicle.currentVolume())")
	@Mapping(target = "remainingPayloadKg", expression = "java(packedVehicle.remainingPayload().kilograms())")
	@Mapping(target = "volumeUtilization", expression = "java(packedVehicle.volumeUtilization())")
	@Mapping(target = "weightUtilization", expression = "java(packedVehicle.weightUtilization())")
	PackedVehicleResponse toResponse(PackedVehicle packedVehicle);

	@Mapping(target = "packedPackagesCount", source = "statistics.packedPackagesCount")
	@Mapping(target = "vehiclesUsed", source = "statistics.vehiclesUsed")
	@Mapping(target = "fullyPacked", source = "statistics.fullyPacked")
	@Mapping(target = "averageVolumeUtilization", source = "statistics.averageVolumeUtilization")
	@Mapping(target = "averageWeightUtilization", source = "statistics.averageWeightUtilization")
	PackingRunResponse toResponse(PackingReport report);
}
