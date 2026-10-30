package org.example.packing.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnKafkaEnabled
public class KafkaTopicConfiguration {

	@Bean
	public NewTopic packageCreateRequestsTopic(final KafkaTopicsProperties topics) {
		return topic(topics.packageCreateRequests(), topics.partitions());
	}

	@Bean
	public NewTopic packageCreatedEventsTopic(final KafkaTopicsProperties topics) {
		return topic(topics.packageCreatedEvents(), topics.partitions());
	}

	private static NewTopic topic(final String name, final int partitions) {
		return TopicBuilder
				.name(name)
				.partitions(partitions)
				.replicas(1)
				.build();
	}
}
