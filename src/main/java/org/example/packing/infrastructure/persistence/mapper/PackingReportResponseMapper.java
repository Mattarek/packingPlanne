package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.dto.PackedVehicleResponse;
import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.dto.PackingRunResponse;
import org.example.packing.application.dto.PlacedPackageResponse;
import org.example.packing.application.dto.PositionResponse;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.domain.model.PackedVehicle;
import org.example.packing.domain.model.PlacedPackage;
import org.example.packing.domain.model.Position;
import org.example.packing.domain.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PackingReportResponseMapper {

	@Mapping(target = "fullyPacked", source = "fullyPacked")
	@Mapping(target = "packedPackagesCount", source = "packedPackagesCount")
	@Mapping(target = "vehiclesUsed", source = "vehiclesUsed")
	@Mapping(target = "averageVolumeUtilization", source = "averageVolumeUtilization")
	@Mapping(target = "averageWeightUtilization", source = "averageWeightUtilization")
	PackingRunResponse toResponse(PackingReport report);

	@Mapping(target = "currentWeight", source = "currentWeight.kilograms")
	@Mapping(target = "currentVolume", source = "currentVolume")
	@Mapping(target = "remainingPayload", source = "remainingPayload.kilograms")
	@Mapping(target = "volumeUtilization", source = "volumeUtilization")
	@Mapping(target = "weightUtilization", source = "weightUtilization")
	PackedVehicleResponse toResponse(PackedVehicle packedVehicle);

	@Mapping(target = "pkg", source = "pkg")
	@Mapping(target = "position", source = "position")
	PlacedPackageResponse toResponse(PlacedPackage placedPackage);

	@Mapping(target = "length", source = "cargoArea.length")
	@Mapping(target = "width", source = "cargoArea.width")
	@Mapping(target = "height", source = "cargoArea.height")
	@Mapping(target = "maxPayload", source = "maxPayload.kilograms")
	VehicleResponse toResponse(Vehicle vehicle);

	@Mapping(target = "length", source = "dimensions.length")
	@Mapping(target = "width", source = "dimensions.width")
	@Mapping(target = "height", source = "dimensions.height")
	@Mapping(target = "weight", source = "weight.kilograms")
	PackageResponse toResponse(Package pkg);

	PositionResponse toResponse(Position position);
}