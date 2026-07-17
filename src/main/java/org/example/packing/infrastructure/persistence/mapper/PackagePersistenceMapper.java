package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PackagePersistenceMapper {
	@Mapping(target = "dimensions", source = "entity")
	@Mapping(target = "weight", source = "weight")
	Package toDomain(PackageEntity entity);

	List<Package> toDomainList(List<PackageEntity> entities);

	@Mapping(target = "id", ignore = true)
	PackageEntity toEntity(PackageRequest request);

	List<PackageEntity> toEntityList(List<PackageRequest> requests);

	@Mapping(target = "length", source = "length")
	@Mapping(target = "width", source = "width")
	@Mapping(target = "height", source = "height")
	Dimensions toDimensions(PackageEntity entity);

	PackageResponse toResponse(PackageEntity entity);

	List<PackageResponse> toResponseList(List<PackageEntity> entities);

	default Weight toWeight(final double kilograms) {
		return new Weight(kilograms);
	}
}