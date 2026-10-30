package org.example.packing.application.port.out;

import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Vehicle;

import java.util.List;

public interface PackingInputLoader {

	List<Package> loadPackages();

	List<Vehicle> loadVehicles();
}
