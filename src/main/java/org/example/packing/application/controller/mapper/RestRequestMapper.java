package org.example.packing.application.controller.mapper;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.command.CreateVehicleCommand;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.mapper.PackingMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * REST request bodies -> use case commands. Changes only when the request JSON changes.
 */
@Mapper(config = PackingMapperConfig.class)
public interface RestRequestMapper {

	CreatePackageCommand toCommand(PackageRequest request);

	List<CreatePackageCommand> toPackageCommands(List<PackageRequest> requests);

	CreateVehicleCommand toCommand(VehicleRequest request);

	List<CreateVehicleCommand> toVehicleCommands(List<VehicleRequest> requests);
}
