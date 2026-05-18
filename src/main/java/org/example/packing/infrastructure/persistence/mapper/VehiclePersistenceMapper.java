package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.VehicleEntity;

// Zmien na mapstract
public final class VehiclePersistenceMapper {

	private VehiclePersistenceMapper() {
	}

	public static Vehicle toDomain(final VehicleEntity entity) {
		return new Vehicle(
				entity.getId(),
				entity.getName(),
				new Dimensions(
						entity.getLengthCm(),
						entity.getWidthCm(),
						entity.getHeightCm()
				),
				new Weight(entity.getMaxPayloadKg())
		);
	}
}