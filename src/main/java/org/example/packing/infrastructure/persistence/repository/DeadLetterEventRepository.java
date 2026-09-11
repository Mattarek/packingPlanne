package org.example.packing.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.example.packing.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.example.packing.infrastructure.persistence.entity.DeadLetterEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeadLetterEventRepository
		extends JpaRepository<DeadLetterEventEntity, UUID> {

	Optional<DeadLetterEventEntity> findByEventId(UUID eventId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select event
			from DeadLetterEventEntity event
			where event.status = :status
			order by event.lastFailedAt asc
			""")
	List<DeadLetterEventEntity> findNextBatch(
			DeadLetterEventStatus status,
			Pageable pageable
	);
}
