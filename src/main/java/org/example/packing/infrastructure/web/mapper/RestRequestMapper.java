package org.example.packing.infrastructure.web.mapper;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.command.CreateVehicleCommand;
import org.example.packing.infrastructure.mapping.PackingMapperConfig;
import org.example.packing.infrastructure.web.dto.PackageRequest;
import org.example.packing.infrastructure.web.dto.VehicleRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = PackingMapperConfig.class)
public interface RestRequestMapper {

	CreatePackageCommand toCommand(PackageRequest request);

	List<CreatePackageCommand> toPackageCommands(List<PackageRequest> requests);

	CreateVehicleCommand toCommand(VehicleRequest request);

	List<CreateVehicleCommand> toVehicleCommands(List<VehicleRequest> requests);
}
