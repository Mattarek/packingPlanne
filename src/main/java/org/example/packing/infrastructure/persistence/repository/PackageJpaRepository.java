package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PackageJpaRepository extends JpaRepository<PackageEntity, UUID> {

	@Modifying
	@Query("delete from PackageEntity entity where entity.id = :id")
	int deletePackageById(@Param("id") UUID id);
}
