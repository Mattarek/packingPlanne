package org.example.packing.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.example.packing.infrastructure.messaging.outbox.OutboxEventStatus;
import org.example.packing.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository
		extends JpaRepository<OutboxEventEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
	@Query("""
			select event
			from OutboxEventEntity event
			where event.status = :status
			order by event.createdAt asc
			""")
	List<OutboxEventEntity> findNextBatchSkippingLocked(
			OutboxEventStatus status,
			Pageable pageable
	);

	@Modifying
	@Query("""
			delete from OutboxEventEntity event
			where event.status = :status
			and event.publishedAt < :publishedBefore
			""")
	int deleteByStatusAndPublishedAtBefore(
			OutboxEventStatus status,
			Instant publishedBefore
	);
}