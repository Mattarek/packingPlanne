package org.example.packing.application.service;

import org.example.packing.application.command.CreateVehicleCommand;
import org.example.packing.application.port.PageQuery;
import org.example.packing.application.port.PageResult;
import org.example.packing.application.port.in.VehicleUseCase;
import org.example.packing.application.port.out.IdGenerator;
import org.example.packing.application.port.out.VehicleRepository;
import org.example.packing.domain.exception.VehicleNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VehicleService implements VehicleUseCase {

	private final VehicleRepository vehicleRepository;
	private final IdGenerator idGenerator;

	public VehicleService(
			final VehicleRepository vehicleRepository,
			final IdGenerator idGenerator
	) {
		this.vehicleRepository = vehicleRepository;
		this.idGenerator = idGenerator;
	}

	@Override
	@Transactional
	public List<Vehicle> createVehicles(final List<CreateVehicleCommand> commands) {
		final List<Vehicle> vehicles = commands.stream()
				.map(this::toVehicle)
				.toList();

		return vehicleRepository.saveAll(vehicles);
	}

	private Vehicle toVehicle(final CreateVehicleCommand command) {
		return new Vehicle(
				idGenerator.newId(),
				command.name(),
				new Dimensions(command.length(), command.width(), command.height()),
				new Weight(command.maxPayload())
		);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<Vehicle> getVehicles(final int page, final int size) {
		return vehicleRepository.findPage(new PageQuery(page, size));
	}

	@Override
	@Transactional(readOnly = true)
	public Vehicle getVehicle(final UUID id) {
		return vehicleRepository.findById(id)
				.orElseThrow(() -> new VehicleNotFoundException(id));
	}

	@Override
	@Transactional
	public void deleteVehicle(final UUID id) {
		if (!vehicleRepository.deleteById(id)) {
			throw new VehicleNotFoundException(id);
		}
	}
}
