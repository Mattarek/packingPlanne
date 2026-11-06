package org.example.packing.infrastructure.messaging.inbox;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record InboxMessage(
		UUID id,
		UUID eventId,
		String eventType,
		int eventVersion,
		String sourceTopic,
		String payload,
		Instant receivedAt,
		int attempts,
		UUID claimToken
) {

	public InboxMessage {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(eventId, "eventId must not be null");
		Objects.requireNonNull(eventType, "eventType must not be null");
		Objects.requireNonNull(sourceTopic, "sourceTopic must not be null");
		Objects.requireNonNull(payload, "payload must not be null");
		Objects.requireNonNull(receivedAt, "receivedAt must not be null");
		if (attempts < 0) {
			throw new IllegalArgumentException("attempts must not be negative: " + attempts);
		}
	}

	public static InboxMessage received(final UUID id, final IncomingMessage incoming, final Instant receivedAt) {
		return new InboxMessage(
				id,
				incoming.eventId(),
				incoming.eventType(),
				incoming.eventVersion(),
				incoming.sourceTopic(),
				incoming.payload(),
				receivedAt,
				0,
				null
		);
	}

	public int attemptsAfterFailure() {
		return attempts + 1;
	}
}
