package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehiclePersistenceMapper {

	@Mapping(target = "cargoArea", source = ".")
	@Mapping(target = "maxPayload", source = "maxPayload")
	Vehicle toDomain(VehiclesEntity entity);

	List<Vehicle> toDomainList(List<VehiclesEntity> entities);

	VehiclesEntity toEntity(VehicleRequest request);

	List<VehiclesEntity> toEntityList(final List<VehicleRequest> requests);

	VehicleResponse toResponse(VehiclesEntity entity);

	List<VehicleResponse> toResponseList(List<VehiclesEntity> entities);

	Dimensions toDimensions(VehiclesEntity entity);

	default Weight toWeight(final double kilograms) {
		return new Weight(kilograms);
	}
}