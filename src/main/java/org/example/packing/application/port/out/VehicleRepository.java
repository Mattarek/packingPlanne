package org.example.packing.application.port.out;

import org.example.packing.application.port.PageQuery;
import org.example.packing.application.port.PageResult;
import org.example.packing.domain.model.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {

	List<Vehicle> saveAll(List<Vehicle> vehicles);

	Optional<Vehicle> findById(UUID id);

	PageResult<Vehicle> findPage(PageQuery query);

	boolean deleteById(UUID id);
}
