package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.application.service.PackageCreateRequestProcessor;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class PackageCreateRequestConsumer {

	private final ObjectMapper objectMapper;
	private final PackageCreateRequestProcessor processor;

	public PackageCreateRequestConsumer(
			final ObjectMapper objectMapper,
			final PackageCreateRequestProcessor processor
	) {
		this.objectMapper = objectMapper;
		this.processor = processor;
	}

	@KafkaListener(
			topics = "${app.kafka.topics.package-create-requests}",
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void consume(final String message) {
		final PackageCreateRequestEvent event =
				parseMessage(message);

		processor.process(event);
	}

	private PackageCreateRequestEvent parseMessage(
			final String message
	) {
		try {
			return objectMapper.readValue(
					message,
					PackageCreateRequestEvent.class
			);
		} catch (final JacksonException exception) {
			throw new NonRetryableKafkaProcessingException(
					"Invalid package create request Kafka message.",
					exception
			);
		}
	}
}