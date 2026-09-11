package org.example.packing.infrastructure.kafka.support;

import org.springframework.context.ConfigurableApplicationContext;

public abstract class ContainerSetupHandler {

	private ContainerSetupHandler next;

	public final ContainerSetupHandler linkWith(
			final ContainerSetupHandler next
	) {
		this.next = next;

		return next;
	}

	public final void handle(final ConfigurableApplicationContext context) {
		setUp(context);

		if (next != null) {
			next.handle(context);
		}
	}

	protected abstract void setUp(ConfigurableApplicationContext context);
}
