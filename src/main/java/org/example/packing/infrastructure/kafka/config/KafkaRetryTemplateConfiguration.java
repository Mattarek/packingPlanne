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
