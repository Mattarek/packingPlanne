package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehiclePersistenceMapper {

	@Mapping(target = "cargoArea", source = ".")
	@Mapping(target = "maxPayload", source = "maxPayloadKg")
	Vehicle toDomain(VehicleEntity entity);

	List<Vehicle> toDomainList(List<VehicleEntity> entities);

	default VehicleEntity toEntity(final VehicleRequest request) {
		return new VehicleEntity(
				request.id(),
				request.name(),
				request.lengthCm(),
				request.widthCm(),
				request.heightCm(),
				request.maxPayloadKg()
		);
	}

	default List<VehicleEntity> toEntityList(final List<VehicleRequest> requests) {
		return requests.stream()
				.map(this::toEntity)
				.toList();
	}

	VehicleResponse toResponse(VehicleEntity entity);

	List<VehicleResponse> toResponseList(List<VehicleEntity> entities);

	default Dimensions toDimensions(final VehicleEntity entity) {
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