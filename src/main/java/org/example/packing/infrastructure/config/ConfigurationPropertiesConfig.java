package org.example.packing.infrastructure.config;

import org.example.packing.infrastructure.kafka.config.KafkaTopicsProperties;
import org.example.packing.infrastructure.messaging.deadletter.DeadLetterProperties;
import org.example.packing.infrastructure.messaging.outbox.OutboxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
		PackageAcceptanceProperties.class,
		PackingStrategyProperties.class,
		OutboxProperties.class,
		DeadLetterProperties.class,
		KafkaTopicsProperties.class
})
public class ConfigurationPropertiesConfig {
}
