package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.mapper.PackingMapperConfig;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Persistence <-> domain only. Changes only when the table or the domain model changes.
 */
@Mapper(config = PackingMapperConfig.class)
public interface VehicleEntityMapper {

	@Mapping(target = "cargoArea", source = ".")
	@Mapping(target = "maxPayload", source = "maxPayload")
	Vehicle toDomain(VehiclesEntity entity);

	List<Vehicle> toDomainList(List<VehiclesEntity> entities);

	@Mapping(target = "length", source = "cargoArea.length")
	@Mapping(target = "width", source = "cargoArea.width")
	@Mapping(target = "height", source = "cargoArea.height")
	@Mapping(target = "maxPayload", source = "maxPayload.kilograms")
	VehiclesEntity toEntity(Vehicle vehicle);

	List<VehiclesEntity> toEntityList(List<Vehicle> vehicles);

	Dimensions toDimensions(VehiclesEntity entity);

	default Weight toWeight(final double kilograms) {
		return new Weight(kilograms);
	}
}
