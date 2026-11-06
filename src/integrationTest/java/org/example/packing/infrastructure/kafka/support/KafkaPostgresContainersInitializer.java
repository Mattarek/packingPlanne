package org.example.packing.infrastructure.kafka.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class KafkaPostgresContainersInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(final ConfigurableApplicationContext context) {
		final ContainerSetupHandler chain = new KafkaContainerHandler();
		chain.linkWith(new PostgresContainerHandler());

		chain.handle(context);
	}
}
