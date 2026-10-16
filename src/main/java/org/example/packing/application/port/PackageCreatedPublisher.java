package org.example.packing.application.port;

import org.example.packing.domain.model.Package;

import java.util.List;

public interface PackageCreatedPublisher {

	void publish(List<Package> createdPackages);
}
