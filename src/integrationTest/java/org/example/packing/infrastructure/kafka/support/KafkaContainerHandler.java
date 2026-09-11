package org.example.packing.infrastructure.kafka.support;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.kafka.KafkaContainer;

public final class KafkaContainerHandler extends ContainerSetupHandler {

	private static final KafkaContainer KAFKA =
			new KafkaContainer("apache/kafka-native:3.8.0");

	static {
		KAFKA.start();
	}

	public static KafkaContainer container() {
		return KAFKA;
	}

	@Override
	protected void setUp(final ConfigurableApplicationContext context) {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
				context,
				"spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers()
		);
	}
}
