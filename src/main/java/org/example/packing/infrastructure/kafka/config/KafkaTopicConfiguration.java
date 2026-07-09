package org.example.packing.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(
		name = "app.kafka.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class KafkaTopicConfiguration {

	@Bean
	public NewTopic packageCreateRequestsTopic(
			@Value(
					"${app.kafka.topics.package-create-requests}"
			) final String topicName
	) {
		return TopicBuilder
				.name(topicName)
				.partitions(1)
				.replicas(1)
				.build();
	}

	@Bean
	public NewTopic packageCreateRequestsDltTopic(
			@Value(
					"${app.kafka.topics.package-create-requests-dlt}"
			) final String topicName
	) {
		return TopicBuilder
				.name(topicName)
				.partitions(1)
				.replicas(1)
				.build();
	}
}