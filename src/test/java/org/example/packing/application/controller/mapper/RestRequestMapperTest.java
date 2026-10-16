package org.example.packing.application.controller.mapper;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.command.CreateVehicleCommand;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RestRequestMapperTest {

	private final RestRequestMapper mapper = Mappers.getMapper(RestRequestMapper.class);

	@Test
	void shouldMapPackageRequestsToCommands() {
		// given
		final PackageRequest request = new PackageRequest(
				10.0, 20.0, 30.0, 5.5, ProductCategory.FOOD, FragilityLevel.FRAGILE
		);

		// when
		final List<CreatePackageCommand> result = mapper.toPackageCommands(List.of(request));

		// then
		assertThat(result).containsExactly(new CreatePackageCommand(
				10.0, 20.0, 30.0, 5.5, ProductCategory.FOOD, FragilityLevel.FRAGILE
		));
	}

	@Test
	void shouldMapVehicleRequestsToCommands() {
		// given
		final VehicleRequest request = new VehicleRequest("Van", 420.0, 180.0, 200.0, 1000.0);

		// when
		final List<CreateVehicleCommand> result = mapper.toVehicleCommands(List.of(request));

		// then
		assertThat(result).containsExactly(new CreateVehicleCommand("Van", 420.0, 180.0, 200.0, 1000.0));
	}
}
