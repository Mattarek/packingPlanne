package org.example.packing.infrastructure.messaging.inbox;

public interface InboxMessageHandler {

	String eventType();

	void handle(InboxMessage message);
}
