package org.example.packing.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class KafkaConsumerConfiguration {

	@Bean
	public ConsumerFactory<String, String> kafkaConsumerFactory(
			@Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers,
			@Value("${spring.kafka.consumer.group-id}") final String groupId
	) {
		final Map<String, Object> properties = new HashMap<>();

		properties.put(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				bootstrapServers
		);

		properties.put(
				ConsumerConfig.GROUP_ID_CONFIG,
				groupId
		);

		properties.put(
				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class
		);

		properties.put(
				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class
		);

		properties.put(
				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
				"earliest"
		);

		return new DefaultKafkaConsumerFactory<>(properties);
	}

	@Bean(name = "kafkaListenerContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
			final ConsumerFactory<String, String> kafkaConsumerFactory
	) {
		final ConcurrentKafkaListenerContainerFactory<String, String> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(kafkaConsumerFactory);

		return factory;
	}
}