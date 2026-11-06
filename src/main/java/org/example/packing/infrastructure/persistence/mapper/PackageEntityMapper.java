package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.mapping.PackingMapperConfig;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = PackingMapperConfig.class)
public interface PackageEntityMapper {

	@Mapping(target = "dimensions", source = "entity")
	@Mapping(target = "weight", source = "weight")
	Package toDomain(PackageEntity entity);

	List<Package> toDomainList(List<PackageEntity> entities);

	@Mapping(target = "length", source = "dimensions.length")
	@Mapping(target = "width", source = "dimensions.width")
	@Mapping(target = "height", source = "dimensions.height")
	@Mapping(target = "weight", source = "weight.kilograms")
	PackageEntity toEntity(Package pkg);

	List<PackageEntity> toEntityList(List<Package> packages);

	Dimensions toDimensions(PackageEntity entity);

	default Weight toWeight(final double kilograms) {
		return new Weight(kilograms);
	}
}
