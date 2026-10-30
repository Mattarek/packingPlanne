package org.example.packing.infrastructure.messaging.inbox;

import java.time.Instant;
import java.util.UUID;

public interface InboxStore {

	boolean registerIfAbsent(UUID id, UUID eventId, String eventType, Instant receivedAt);
}
