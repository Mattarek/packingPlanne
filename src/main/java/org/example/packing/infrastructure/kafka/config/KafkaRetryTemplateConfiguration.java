package org.example.packing.infrastructure.kafka.config;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.Map;

/**
 * Re-declares the app's single {@link KafkaTemplate}, so its value serializer
 * can handle both kinds of values it's actually asked to publish:
 * <ul>
 *   <li>plain, pre-serialized JSON strings — {@link org.example.packing.infrastructure.kafka.producer.OutboxEventRelay}
 *       publishing outbox payloads, and</li>
 *   <li>deserialized listener objects — Spring Kafka's {@code @RetryableTopic}
 *       republishing a {@link PackageCreateRequestEvent} to a retry topic or
 *       the DLT after a business-exception failure in
 *       {@link org.example.packing.infrastructure.kafka.consumer.PackageCreateRequestConsumer}.</li>
 * </ul>
 * Spring Boot's auto-configured default (String-only) template can't do the
 * second one — republishing a POJO through a {@code StringSerializer} throws
 * {@code SerializationException} and the retry/DLT publish silently fails.
 * <p>
 * {@link DelegatingByTypeSerializer} picks the right delegate by the runtime
 * class of the value being sent, so a single template (and the same bean name
 * Boot would have used) covers both cases — no need to point
 * {@code @RetryableTopic(kafkaTemplate = ...)} at a second bean, which is
 * deliberately avoided here.
 */
@Configuration
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class KafkaRetryTemplateConfiguration {

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(
			final KafkaProperties kafkaProperties
	) {
		final Serializer<Object> valueSerializer = new DelegatingByTypeSerializer(
				Map.of(
						String.class, new StringSerializer(),
						PackageCreateRequestEvent.class, new JacksonJsonSerializer<>(),
						// A record that fails to deserialize (ErrorHandlingDeserializer) is
						// republished to the DLT as the original raw bytes, extracted from
						// this wrapper — send them back out unchanged rather than trying
						// (and failing) to JSON-serialize the exception itself.
						DeserializationException.class,
						(Serializer<DeserializationException>) (topic, exception) -> exception.getData(),
						byte[].class, new ByteArraySerializer()
				)
		);

		final ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(
				kafkaProperties.buildProducerProperties(),
				new StringSerializer(),
				valueSerializer
		);

		return new KafkaTemplate<>(producerFactory);
	}
}
