package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.mapping.PackingMapperConfig;
import org.example.packing.infrastructure.persistence.entity.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = PackingMapperConfig.class)
public interface VehicleEntityMapper {

	@Mapping(target = "cargoArea", source = ".")
	@Mapping(target = "maxPayload", source = "maxPayload")
	Vehicle toDomain(VehicleEntity entity);

	List<Vehicle> toDomainList(List<VehicleEntity> entities);

	@Mapping(target = "length", source = "cargoArea.length")
	@Mapping(target = "width", source = "cargoArea.width")
	@Mapping(target = "height", source = "cargoArea.height")
	@Mapping(target = "maxPayload", source = "maxPayload.kilograms")
	VehicleEntity toEntity(Vehicle vehicle);

	List<VehicleEntity> toEntityList(List<Vehicle> vehicles);

	Dimensions toDimensions(VehicleEntity entity);

	default Weight toWeight(final double kilograms) {
		return new Weight(kilograms);
	}
}
