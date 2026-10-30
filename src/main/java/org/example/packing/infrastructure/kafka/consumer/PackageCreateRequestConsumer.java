package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
import org.example.packing.infrastructure.kafka.config.KafkaTopicsProperties;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.example.packing.infrastructure.messaging.deadletter.DeadLetterRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnKafkaEnabled
public class PackageCreateRequestConsumer {

	private static final Logger log = LoggerFactory.getLogger(PackageCreateRequestConsumer.class);

	private static final String EVENT_TYPE = "PACKAGE_CREATE_REQUESTED";

	private final PackageCreateRequestProcessor processor;
	private final DeadLetterRecorder deadLetterRecorder;
	private final String topicName;

	public PackageCreateRequestConsumer(
			final PackageCreateRequestProcessor processor,
			final DeadLetterRecorder deadLetterRecorder,
			final KafkaTopicsProperties topics
	) {
		this.processor = processor;
		this.deadLetterRecorder = deadLetterRecorder;
		this.topicName = topics.packageCreateRequests();
	}

	@RetryableTopic(
			attempts = "${app.kafka.retry.attempts}",
			backOff = @BackOff(delayString = "${app.kafka.retry.backoff-ms}"),
			exclude = NonRetryableKafkaProcessingException.class,
			dltTopicSuffix = ".DLT",
			numPartitions = "${app.kafka.topics.partitions}",
			autoCreateTopics = "true"
	)
	@KafkaListener(
			topics = "${app.kafka.topics.package-create-requests}",
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void consume(final PackageCreateRequestEvent event) {
		processor.process(event);
	}

	@DltHandler
	public void handleDlt(
			final PackageCreateRequestEvent event,
			@Header(KafkaHeaders.EXCEPTION_MESSAGE) final String exceptionMessage
	) {
		// event is null when the record itself is a poison pill (e.g. malformed
		// JSON): deserialization fails again on the DLT topic itself, there is
		// no eventId to key a dead-letter row on, so this stays log-only.
		final UUID eventId = event == null ? null : event.eventId();

		log.error(
				"Package create event sent to DLT: eventId={}, reason={}",
				eventId,
				exceptionMessage
		);

		deadLetterRecorder.record(eventId, EVENT_TYPE, topicName, event, exceptionMessage);
	}
}
