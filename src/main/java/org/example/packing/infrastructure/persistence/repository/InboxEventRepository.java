package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.persistence.entity.InboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface InboxEventRepository
		extends JpaRepository<InboxEventEntity, UUID> {

	@Modifying
	@Transactional
	@Query(
			value = """
					INSERT INTO inbox_events (
					    id,
					    event_id,
					    event_type,
					    received_at
					)
					VALUES (
					    :id,
					    :eventId,
					    :eventType,
					    :receivedAt
					)
					ON CONFLICT (event_id) DO NOTHING
					""",
			nativeQuery = true
	)
	int insertIgnoringDuplicate(
			@Param("id") UUID id,
			@Param("eventId") UUID eventId,
			@Param("eventType") String eventType,
			@Param("receivedAt") Instant receivedAt
	);

	@Modifying
	@Transactional
	@Query(
			value = "DELETE FROM inbox_events WHERE event_id = :eventId",
			nativeQuery = true
	)
	int deleteByEventIdNative(@Param("eventId") UUID eventId);

	@Modifying
	@Transactional
	@Query(
			value = """
					DELETE FROM inbox_events
					WHERE id IN (
					    SELECT id
					    FROM inbox_events
					    WHERE received_at < :receivedBefore
					    ORDER BY received_at
					    LIMIT :limit
					    FOR UPDATE SKIP LOCKED
					)
					""",
			nativeQuery = true
	)
	int deleteChunkReceivedBefore(
			@Param("receivedBefore") Instant receivedBefore,
			@Param("limit") int limit
	);
}
