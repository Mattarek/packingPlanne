package org.example.packing.application.service;

import org.example.packing.application.command.CreateVehicleCommand;
import org.example.packing.application.port.IdGenerator;
import org.example.packing.application.port.VehicleRepository;
import org.example.packing.domain.exception.VehicleNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Vehicle;
import org.example.packing.domain.model.Weight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {

	private final VehicleRepository vehicleRepository;
	private final IdGenerator idGenerator;

	public VehicleService(
			final VehicleRepository vehicleRepository,
			final IdGenerator idGenerator
	) {
		this.vehicleRepository = vehicleRepository;
		this.idGenerator = idGenerator;
	}

	@Transactional
	public List<Vehicle> createVehicles(final List<CreateVehicleCommand> commands) {
		// Building the domain Vehicle enforces its invariants (Dimensions, Weight).
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

	@Transactional(readOnly = true)
	public Page<Vehicle> getVehicles(final int page, final int size) {
		final Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.by("id").ascending()
		);

		return vehicleRepository.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public Vehicle getVehicle(final UUID id) {
		return vehicleRepository.findById(id)
				.orElseThrow(() -> new VehicleNotFoundException(id));
	}

	@Transactional
	public void deleteVehicle(final UUID id) {
		if (!vehicleRepository.existsById(id)) {
			throw new VehicleNotFoundException(id);
		}

		vehicleRepository.deleteById(id);
	}
}
