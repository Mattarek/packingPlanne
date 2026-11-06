package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.infrastructure.kafka.config.KafkaTopicsProperties;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.KafkaExceptionTranslator;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestHandlerRegistry;
import org.example.packing.infrastructure.messaging.PayloadSerializer;
import org.example.packing.infrastructure.messaging.inbox.InboxAcceptor;
import org.example.packing.infrastructure.messaging.inbox.IncomingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PackageCreateRequestInboxWriter {

	private static final Logger log = LoggerFactory.getLogger(PackageCreateRequestInboxWriter.class);

	private final KafkaEventValidator eventValidator;
	private final PackageCreateRequestHandlerRegistry handlerRegistry;
	private final PayloadSerializer payloadSerializer;
	private final InboxAcceptor inboxAcceptor;
	private final KafkaExceptionTranslator exceptionTranslator;
	private final String topicName;

	public PackageCreateRequestInboxWriter(
			final KafkaEventValidator eventValidator,
			final PackageCreateRequestHandlerRegistry handlerRegistry,
			final PayloadSerializer payloadSerializer,
			final InboxAcceptor inboxAcceptor,
			final KafkaExceptionTranslator exceptionTranslator,
			final KafkaTopicsProperties topics
	) {
		this.eventValidator = eventValidator;
		this.handlerRegistry = handlerRegistry;
		this.payloadSerializer = payloadSerializer;
		this.inboxAcceptor = inboxAcceptor;
		this.exceptionTranslator = exceptionTranslator;
		this.topicName = topics.packageCreateRequests();
	}

	public void write(final PackageCreateRequestEvent event) {
		eventValidator.validate(event);
		handlerRegistry.handlerFor(event);

		final IncomingMessage incoming = new IncomingMessage(
				event.eventId(),
				event.eventType(),
				event.version(),
				topicName,
				payloadSerializer.serialize(event)
		);

		exceptionTranslator.runTranslatingExceptions(event.eventId(), () -> {
			if (!inboxAcceptor.accept(incoming)) {
				log.info("Ignoring duplicated Kafka event: eventId={}", event.eventId());
			}
		});
	}
}
