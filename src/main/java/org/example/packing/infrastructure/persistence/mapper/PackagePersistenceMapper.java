package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PackagePersistenceMapper {

	@Mapping(target = "dimensions", source = ".")
	@Mapping(target = "weight", source = "weightKg")
	Package toDomain(PackageEntity entity);

	default Dimensions toDimensions(final PackageEntity entity) {
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