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
			) final String topicName,
			@Value(
					"${app.kafka.topics.partitions:3}"
			) final int partitions
	) {
		return TopicBuilder
				.name(topicName)
				.partitions(partitions)
				.replicas(1)
				.build();
	}

	@Bean
	public NewTopic packageCreatedEventsTopic(
			@Value(
					"${app.kafka.topics.package-created-events}"
			) final String topicName,
			@Value(
					"${app.kafka.topics.partitions:3}"
			) final int partitions
	) {
		return TopicBuilder
				.name(topicName)
				.partitions(partitions)
				.replicas(1)
				.build();
	}
}