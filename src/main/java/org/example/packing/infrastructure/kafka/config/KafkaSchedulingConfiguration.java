package org.example.packing.infrastructure.kafka.config;

import org.example.packing.infrastructure.config.ConditionalOnKafkaEnabled;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnKafkaEnabled
public class KafkaSchedulingConfiguration {
}
