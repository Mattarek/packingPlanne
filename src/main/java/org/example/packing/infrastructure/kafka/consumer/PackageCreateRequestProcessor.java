package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.KafkaExceptionTranslator;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestHandler;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestHandlerRegistry;
import org.example.packing.infrastructure.messaging.inbox.InboxDeduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PackageCreateRequestProcessor {

	private static final Logger log = LoggerFactory.getLogger(PackageCreateRequestProcessor.class);

	private final KafkaEventValidator eventValidator;
	private final PackageCreateRequestHandlerRegistry handlerRegistry;
	private final InboxDeduplicator inboxDeduplicator;
	private final KafkaExceptionTranslator exceptionTranslator;

	public PackageCreateRequestProcessor(
			final KafkaEventValidator eventValidator,
			final PackageCreateRequestHandlerRegistry handlerRegistry,
			final InboxDeduplicator inboxDeduplicator,
			final KafkaExceptionTranslator exceptionTranslator
	) {
		this.eventValidator = eventValidator;
		this.handlerRegistry = handlerRegistry;
		this.inboxDeduplicator = inboxDeduplicator;
		this.exceptionTranslator = exceptionTranslator;
	}

	@Transactional
	public void process(final PackageCreateRequestEvent event) {
		eventValidator.validate(event);

		final PackageCreateRequestHandler handler = handlerRegistry.handlerFor(event);

		exceptionTranslator.runTranslatingExceptions(event.eventId(), () -> {
			if (!inboxDeduplicator.registerFirstDelivery(event.eventId(), event.eventType())) {
				log.info("Ignoring duplicated Kafka event: eventId={}", event.eventId());
				return;
			}

			handler.handle(event);
		});
	}
}
