package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.persistence.entity.InboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface InboxEventRepository
		extends JpaRepository<InboxEventEntity, UUID> {

	@Modifying
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
}