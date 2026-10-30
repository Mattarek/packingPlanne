package org.example.packing.application.port.out;

import org.example.packing.application.port.PageQuery;
import org.example.packing.application.port.PageResult;
import org.example.packing.domain.model.Package;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PackageRepository {

	List<Package> saveAll(List<Package> packages);

	Optional<Package> findById(UUID id);

	PageResult<Package> findPage(PageQuery query);

	boolean deleteById(UUID id);
}
