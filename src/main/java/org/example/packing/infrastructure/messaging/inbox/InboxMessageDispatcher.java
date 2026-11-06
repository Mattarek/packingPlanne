package org.example.packing.infrastructure.messaging.inbox;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InboxMessageDispatcher {

	private final Map<String, InboxMessageHandler> handlersByEventType;

	public InboxMessageDispatcher(final List<InboxMessageHandler> handlers) {
		this.handlersByEventType = handlers.stream()
				.collect(Collectors.toUnmodifiableMap(InboxMessageHandler::eventType, Function.identity()));
	}

	public void dispatch(final InboxMessage message) {
		Optional.ofNullable(handlersByEventType.get(message.eventType()))
				.orElseThrow(() -> new PermanentInboxFailureException(
						"No inbox handler for event type: " + message.eventType()
				))
				.handle(message);
	}
}
