package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.infrastructure.kafka.event.EventType;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.KafkaExceptionTranslator;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestHandlerRegistry;
import org.example.packing.infrastructure.messaging.PayloadDeserializationException;
import org.example.packing.infrastructure.messaging.PayloadDeserializer;
import org.example.packing.infrastructure.messaging.inbox.InboxMessage;
import org.example.packing.infrastructure.messaging.inbox.InboxMessageHandler;
import org.example.packing.infrastructure.messaging.inbox.PermanentInboxFailureException;
import org.springframework.stereotype.Component;

@Component
public class PackageCreateRequestInboxHandler implements InboxMessageHandler {

	private final PayloadDeserializer payloadDeserializer;
	private final PackageCreateRequestHandlerRegistry handlerRegistry;
	private final KafkaExceptionTranslator exceptionTranslator;

	public PackageCreateRequestInboxHandler(
			final PayloadDeserializer payloadDeserializer,
			final PackageCreateRequestHandlerRegistry handlerRegistry,
			final KafkaExceptionTranslator exceptionTranslator
	) {
		this.payloadDeserializer = payloadDeserializer;
		this.handlerRegistry = handlerRegistry;
		this.exceptionTranslator = exceptionTranslator;
	}

	@Override
	public String eventType() {
		return EventType.PACKAGE_CREATE_REQUESTED.wireValue();
	}

	@Override
	public void handle(final InboxMessage message) {
		try {
			final PackageCreateRequestEvent event =
					payloadDeserializer.deserialize(message.payload(), PackageCreateRequestEvent.class);

			exceptionTranslator.runTranslatingExceptions(
					message.eventId(),
					() -> handlerRegistry.handlerFor(event).handle(event)
			);
		} catch (final NonRetryableKafkaProcessingException | PayloadDeserializationException exception) {
			throw new PermanentInboxFailureException(exception.getMessage(), exception);
		}
	}
}
