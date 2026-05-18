package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;

// Zmien na mapstract
public final class PackagePersistenceMapper {

	private PackagePersistenceMapper() {
	}

	public static Package toDomain(final PackageEntity entity) {
		return new Package(
				entity.getId(),
				new Dimensions(
						entity.getLengthCm(),
						entity.getWidthCm(),
						entity.getHeightCm()
				),
				new Weight(entity.getWeightKg())
		);
	}
}