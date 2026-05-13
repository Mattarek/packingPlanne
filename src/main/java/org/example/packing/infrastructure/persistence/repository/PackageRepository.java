package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<PackageEntity, String> {
}
