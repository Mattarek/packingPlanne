package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehiclePersistenceMapper {

	@Mapping(target = "cargoArea", source = ".")
	@Mapping(target = "maxPayload", source = "maxPayload")
	Vehicle toDomain(VehiclesEntity entity);

	List<Vehicle> toDomainList(List<VehiclesEntity> entities);

	default VehiclesEntity toEntity(final VehicleRequest request) {
		return new VehiclesEntity(
				request.id(),
				request.name(),
				request.length(),
				request.width(),
				request.height(),
				request.maxPayload()
		);
	}

	default List<VehiclesEntity> toEntityList(final List<VehicleRequest> requests) {
		return requests.stream()
				.map(this::toEntity)
				.toList();
	}

	@Mapping(target = "length", source = "length")
	@Mapping(target = "width", source = "width")
	@Mapping(target = "height", source = "height")
	@Mapping(target = "maxPayload", source = "maxPayload")
	VehicleResponse toResponse(VehiclesEntity entity);

	List<VehicleResponse> toResponseList(List<VehiclesEntity> entities);

	default Dimensions toDimensions(final VehiclesEntity entity) {
		return new Dimensions(
				entity.getLength(),
				entity.getWidth(),
				entity.getHeight()
		);
	}

	default Weight toWeight(final double weightKg) {
		return new Weight(weightKg);
	}
}