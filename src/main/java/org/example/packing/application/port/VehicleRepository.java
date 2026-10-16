package org.example.packing.application.port;

import org.example.packing.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {

	List<Vehicle> saveAll(List<Vehicle> vehicles);

	Optional<Vehicle> findById(UUID id);

	Page<Vehicle> findAll(Pageable pageable);

	boolean existsById(UUID id);

	void deleteById(UUID id);
}
