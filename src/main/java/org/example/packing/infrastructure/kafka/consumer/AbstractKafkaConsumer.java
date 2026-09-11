package org.example.packing.infrastructure.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKafkaConsumer<T> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected final void handle(final T event) {
		log.debug("Received event: {}", event);

		process(event);
	}

	protected abstract void process(T event);
}

// 1.
// Partycje logiczne w postgresie

// 2. Na co zwrócić uwage, SOLID, wzorce projektowe, Coupling, Cohesion
// Coupling - co to zla/dobra
// Cohesion - co to zla/dobra