package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.infrastructure.persistence.entity.InboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InboxMessageRepository
		extends JpaRepository<InboxMessageEntity, UUID> {

	@Modifying
	@Transactional
	@Query(
			value = """
					INSERT INTO inbox_messages (
					    id,
					    event_id,
					    event_type,
					    event_version,
					    source_topic,
					    payload,
					    received_at,
					    available_at,
					    attempts
					)
					VALUES (
					    :id,
					    :eventId,
					    :eventType,
					    :eventVersion,
					    :sourceTopic,
					    :payload,
					    :receivedAt,
					    :receivedAt,
					    0
					)
					ON CONFLICT (event_id) DO NOTHING
					""",
			nativeQuery = true
	)
	int insertIgnoringDuplicate(
			@Param("id") UUID id,
			@Param("eventId") UUID eventId,
			@Param("eventType") String eventType,
			@Param("eventVersion") int eventVersion,
			@Param("sourceTopic") String sourceTopic,
			@Param("payload") String payload,
			@Param("receivedAt") Instant receivedAt
	);

	@Transactional
	@Query(
			value = """
					UPDATE inbox_messages
					SET claim_token = :claimToken,
					    available_at = :leaseUntil
					WHERE id IN (
					    SELECT id
					    FROM inbox_messages
					    WHERE available_at <= :now
					    ORDER BY available_at
					    LIMIT :limit
					    FOR UPDATE SKIP LOCKED
					)
					RETURNING *
					""",
			nativeQuery = true
	)
	List<InboxMessageEntity> claimAvailable(
			@Param("limit") int limit,
			@Param("now") Instant now,
			@Param("leaseUntil") Instant leaseUntil,
			@Param("claimToken") UUID claimToken
	);

	@Modifying
	@Transactional
	@Query(
			value = "DELETE FROM inbox_messages WHERE id = :id AND claim_token = :claimToken",
			nativeQuery = true
	)
	int deleteClaimed(
			@Param("id") UUID id,
			@Param("claimToken") UUID claimToken
	);

	@Modifying
	@Transactional
	@Query(
			value = """
					UPDATE inbox_messages
					SET attempts = attempts + 1,
					    available_at = :availableAt,
					    claim_token = NULL,
					    last_error = :lastError
					WHERE id = :id
					AND claim_token = :claimToken
					""",
			nativeQuery = true
	)
	int releaseForRetry(
			@Param("id") UUID id,
			@Param("claimToken") UUID claimToken,
			@Param("availableAt") Instant availableAt,
			@Param("lastError") String lastError
	);
}
