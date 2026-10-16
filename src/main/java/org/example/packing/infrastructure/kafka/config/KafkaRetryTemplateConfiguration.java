package org.example.packing.infrastructure.kafka.config;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
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

import java.util.LinkedHashMap;
import java.util.Map;

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
		final Map<Class<?>, Serializer<?>> delegates = new LinkedHashMap<>();
		delegates.put(String.class, new StringSerializer());
		delegates.put(byte[].class, new ByteArraySerializer());
		delegates.put(
				DeserializationException.class,
				(Serializer<DeserializationException>) (topic, exception) -> exception.getData()
		);
		// Fallback for every event object a @RetryableTopic listener forwards to
		// its retry/DLT topics – a new event type needs no change here.
		delegates.put(Object.class, new JacksonJsonSerializer<>());

		// assignable = true: match by isAssignableFrom; the serializer orders
		// delegates most-specific first, so Object.class is only the fallback.
		final Serializer<Object> valueSerializer = new DelegatingByTypeSerializer(delegates, true);

		final ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(
				kafkaProperties.buildProducerProperties(),
				new StringSerializer(),
				valueSerializer
		);

		return new KafkaTemplate<>(producerFactory);
	}
}
