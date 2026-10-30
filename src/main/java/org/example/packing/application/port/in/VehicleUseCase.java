package org.example.packing.application.port.in;

import org.example.packing.application.command.CreateVehicleCommand;
import org.example.packing.application.port.PageResult;
import org.example.packing.domain.model.Vehicle;

import java.util.List;
import java.util.UUID;

public interface VehicleUseCase {

	List<Vehicle> createVehicles(List<CreateVehicleCommand> commands);

	PageResult<Vehicle> getVehicles(int page, int size);

	Vehicle getVehicle(UUID id);

	void deleteVehicle(UUID id);
}
