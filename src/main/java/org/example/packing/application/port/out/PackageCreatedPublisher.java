package org.example.packing.application.port.out;

import org.example.packing.domain.model.Package;

import java.util.List;

public interface PackageCreatedPublisher {

	void publish(List<Package> createdPackages);
}
