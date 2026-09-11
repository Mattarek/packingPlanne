package org.example.packing.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.example.packing.infrastructure.persistence.entity.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository
		extends JpaRepository<OutboxEventEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select event
			from OutboxEventEntity event
			where event.status = :status
			order by event.createdAt asc
			""")
	List<OutboxEventEntity> findNextBatch(
			OutboxEventStatus status,
			Pageable pageable
	);

	boolean existsByEventId(UUID eventId);
}