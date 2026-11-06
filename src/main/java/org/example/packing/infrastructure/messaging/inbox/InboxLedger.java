package org.example.packing.infrastructure.messaging.inbox;

import java.time.Instant;
import java.util.UUID;

public interface InboxLedger {

	boolean registerIfAbsent(UUID id, UUID eventId, String eventType, Instant receivedAt);

	void forget(UUID eventId);

	int deleteReceivedBefore(Instant receivedBefore, int limit);
}
