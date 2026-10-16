package org.example.packing.application.port;

import org.example.packing.domain.model.Package;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PackageRepository {

	List<Package> saveAll(List<Package> packages);

	Optional<Package> findById(UUID id);

	Page<Package> findAll(Pageable pageable);

	boolean existsById(UUID id);

	void deleteById(UUID id);
}
