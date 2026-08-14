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

	/*
	 * Non-blocking retry: błędy niesklasyfikowane jako NonRetryable
	 * (czyli domyślnie wszystko poza NonRetryableKafkaProcessingException,
	 * w tym RetryableKafkaProcessingException) są ponawiane przez
	 * republikację na osobne topiki retry, zamiast blokować partycję
	 * głównego topicu.
	 *
	 * attempts=${app.kafka.retry.attempts:3}: pierwsza próba + N-1 ponowień
	 * (retry-0, retry-1, ...), co ${app.kafka.retry.backoff-ms:5000}ms,
	 * po wyczerpaniu -> package-create-requests.DLT.
	 *
	 * Republikacja na retry/DLT wysyła zdeserializowany
	 * PackageCreateRequestEvent (nie surowe bajty), więc app-owy
	 * KafkaTemplate musi umieć zserializować i String (payloady outboxa),
	 * i ten obiekt — patrz KafkaRetryTemplateConfiguration.
	 */
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

	/*
	 * Explicit DLT handler instead of relying on @RetryableTopic's implicit
	 * default (a framework-provided no-op logging listener) — once records
	 * actually reach the DLT (see the comment on @RetryableTopic above), the
	 * implicit default has proven unreliable in this Spring Boot/Kafka
	 * version combination. Declaring our own keeps behavior predictable and
	 * gives us an actual log line to look for in production.
	 */
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