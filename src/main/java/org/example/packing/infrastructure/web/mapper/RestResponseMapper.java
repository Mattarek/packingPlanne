package org.example.packing.infrastructure.web.mapper;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicleSnapshot;
import org.example.packing.domain.model.PlacedPackage;
import org.example.packing.domain.model.Position;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.infrastructure.mapping.PackingMapperConfig;
import org.example.packing.infrastructure.web.dto.PackageResponse;
import org.example.packing.infrastructure.web.dto.PackedVehicleResponse;
import org.example.packing.infrastructure.web.dto.PackingRunResponse;
import org.example.packing.infrastructure.web.dto.PlacedPackageResponse;
import org.example.packing.infrastructure.web.dto.PositionResponse;
import org.example.packing.infrastructure.web.dto.VehicleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

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

	@Mapping(target = "currentWeightKg", source = "currentWeight.kilograms")
	@Mapping(target = "remainingPayloadKg", source = "remainingPayload.kilograms")
	PackedVehicleResponse toResponse(PackedVehicleSnapshot packedVehicle);

	@Mapping(target = "packedPackagesCount", source = "statistics.packedPackagesCount")
	@Mapping(target = "vehiclesUsed", source = "statistics.vehiclesUsed")
	@Mapping(target = "fullyPacked", source = "statistics.fullyPacked")
	@Mapping(target = "averageVolumeUtilization", source = "statistics.averageVolumeUtilization")
	@Mapping(target = "averageWeightUtilization", source = "statistics.averageWeightUtilization")
	PackingRunResponse toResponse(PackingReport report);
}
