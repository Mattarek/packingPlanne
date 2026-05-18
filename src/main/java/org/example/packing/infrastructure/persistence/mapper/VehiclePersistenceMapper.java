package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehiclePersistenceMapper {

	@Mapping(target = "dimensions", source = ".")
	@Mapping(target = "maxPayload", source = "maxPayloadKg")
	Vehicle toDomain(VehicleEntity entity);

	default Dimensions toDimensions(final VehicleEntity entity) {
		return new Dimensions(
				entity.getLengthCm(),
				entity.getWidthCm(),
				entity.getHeightCm()
		);
	}

	default Weight toWeight(final Double weightKg) {
		return new Weight(weightKg);
	}
}