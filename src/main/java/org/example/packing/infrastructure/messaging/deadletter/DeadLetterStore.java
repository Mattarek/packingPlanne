package org.example.packing.infrastructure.messaging.deadletter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeadLetterStore {

	Optional<DeadLetter> findByEventId(UUID eventId);

	List<DeadLetter> lockNextPendingRetrySkippingLocked(int limit);

	void save(DeadLetter deadLetter);
}
