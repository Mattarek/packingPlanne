package org.example.packing.infrastructure.kafka.config;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
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
@ConditionalOnKafkaEnabled
public class KafkaProducerConfiguration {

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
		delegates.put(Object.class, new JacksonJsonSerializer<>());

		final Serializer<Object> valueSerializer = new DelegatingByTypeSerializer(delegates, true);

		final ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(
				kafkaProperties.buildProducerProperties(),
				new StringSerializer(),
				valueSerializer
		);

		return new KafkaTemplate<>(producerFactory);
	}
}
