package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.persistence.entity.VehiclesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<VehiclesEntity, String> {
}
