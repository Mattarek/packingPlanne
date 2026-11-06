package org.example.packing.infrastructure.messaging.inbox;

import java.util.Objects;
import java.util.UUID;

public record IncomingMessage(
		UUID eventId,
		String eventType,
		int eventVersion,
		String sourceTopic,
		String payload
) {

	public IncomingMessage {
		Objects.requireNonNull(eventId, "eventId must not be null");
		Objects.requireNonNull(eventType, "eventType must not be null");
		Objects.requireNonNull(sourceTopic, "sourceTopic must not be null");
		Objects.requireNonNull(payload, "payload must not be null");
	}
}
