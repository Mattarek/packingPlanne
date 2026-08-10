package org.example.packing.infrastructure.kafka.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class PostgresOnlyContainerInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(final ConfigurableApplicationContext context) {
		new PostgresContainerHandler().handle(context);
	}
}
