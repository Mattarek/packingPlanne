package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.infrastructure.kafka.deadletter.DeadLetterRecorder;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class PackageCreateRequestConsumer extends AbstractKafkaConsumer<PackageCreateRequestEvent> {

	private static final Logger log = LoggerFactory.getLogger(PackageCreateRequestConsumer.class);

	private static final String EVENT_TYPE = "PACKAGE_CREATE_REQUESTED";

	private final PackageCreateRequestProcessor processor;
	private final DeadLetterRecorder deadLetterRecorder;
	private final String topicName;

	public PackageCreateRequestConsumer(
			final PackageCreateRequestProcessor processor,
			final DeadLetterRecorder deadLetterRecorder,
			@Value("${app.kafka.topics.package-create-requests}") final String topicName
	) {
		this.processor = processor;
		this.deadLetterRecorder = deadLetterRecorder;
		this.topicName = topicName;
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

	// Metoda jest uzywana, @DltHandler jest uzywany przez kafke automatycznie przez Springa,
	// przez mechanizm listenerow
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
