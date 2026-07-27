package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.application.service.PackageCreateRequestProcessor;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class PackageCreateRequestConsumer {

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
	 * attempts=3: pierwsza próba + 2 ponowienia (retry-0, retry-1),
	 * co 5s, po wyczerpaniu -> package-create-requests.DLT.
	 */
	@RetryableTopic(
			attempts = "3",
			backOff = @BackOff(delay = 5_000L),
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
		processor.process(event);
	}
}