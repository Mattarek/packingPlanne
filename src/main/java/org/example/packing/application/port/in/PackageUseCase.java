package org.example.packing.application.port.in;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.port.PageResult;
import org.example.packing.domain.model.Package;

import java.util.List;
import java.util.UUID;

public interface PackageUseCase {

	List<Package> createPackages(List<CreatePackageCommand> commands);

	PageResult<Package> getPackages(int page, int size);

	Package getPackage(UUID id);

	void deletePackage(UUID id);
}
