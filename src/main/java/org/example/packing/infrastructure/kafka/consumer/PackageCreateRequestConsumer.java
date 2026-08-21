package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.application.service.PackageCreateRequestProcessor;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class PackageCreateRequestConsumer extends AbstractKafkaConsumer<PackageCreateRequestEvent> {

	private static final Logger log = LoggerFactory.getLogger(PackageCreateRequestConsumer.class);

	private final PackageCreateRequestProcessor processor;

	public PackageCreateRequestConsumer(
			final PackageCreateRequestProcessor processor
	) {
		this.processor = processor;
	}

	@RetryableTopic(
			attempts = "${app.kafka.retry.attempts:3}",
			backOff = @BackOff(delayString = "${app.kafka.retry.backoff-ms:5000}"),
			exclude = NonRetryableKafkaProcessingException.class,
			dltTopicSuffix = ".DLT",
			numPartitions = "${app.kafka.topics.partitions:3}",
			autoCreateTopics = "true"
	)
	@KafkaListener(
			topics = "${app.kafka.topics.package-create-requests}", // czyta z topicu package-create-requests
			groupId = "${spring.kafka.consumer.group-id}" // nalezy do consumer groupy packing-group
	)
	public void consume(final PackageCreateRequestEvent event) {
		handle(event);
	}

	@Override
	protected void process(final PackageCreateRequestEvent event) {
		processor.process(event);
	}

	@DltHandler
	public void handleDlt(
			final PackageCreateRequestEvent event,
			@Header(KafkaHeaders.EXCEPTION_MESSAGE) final String exceptionMessage
	) {
		log.error(
				"Package create event sent to DLT: eventId={}, reason={}",
				event.eventId(),
				exceptionMessage
		);
	}
}