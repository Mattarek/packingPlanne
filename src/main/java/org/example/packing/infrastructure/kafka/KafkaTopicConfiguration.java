package org.example.packing.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
