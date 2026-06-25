package org.example.packing.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class KafkaTopicConfiguration {
	@Bean
	NewTopic packageCreateRequestsTopic() {
		return new NewTopic(
				"package-create-requests",
				1,
				(short) 1
		);
	}
}
