package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PackagePersistenceMapper {

	@Mapping(target = "dimensions", source = ".")
	@Mapping(target = "weight", source = "weightKg")
	Package toDomain(PackageEntity entity);

	List<Package> toDomainList(List<PackageEntity> entities);

	default PackageEntity toEntity(final PackageRequest request) {
		return new PackageEntity(
				request.id(),
				request.lengthCm(),
				request.widthCm(),
				request.heightCm(),
				request.weightKg()
		);
	}

	default List<PackageEntity> toEntityList(final List<PackageRequest> requests) {
		return requests.stream()
				.map(this::toEntity)
				.toList();
	}

	default Dimensions toDimensions(final PackageEntity entity) {
		return new Dimensions(
				entity.getLengthCm(),
				entity.getWidthCm(),
				entity.getHeightCm()
		);
	}

	default Weight toWeight(final double weightKg) {
		return new Weight(weightKg);
	}
}