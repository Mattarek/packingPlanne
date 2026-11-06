package org.example.packing.infrastructure.kafka.producer;

import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
import org.example.packing.infrastructure.messaging.MessageSender;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnKafkaEnabled
public class KafkaMessageSender implements MessageSender {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public KafkaMessageSender(final KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public CompletableFuture<Void> send(final String topic, final String key, final String payload) {
		return kafkaTemplate.send(topic, key, payload).thenApply(result -> null);
	}
}
