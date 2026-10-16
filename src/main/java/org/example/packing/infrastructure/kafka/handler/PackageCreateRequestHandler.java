package org.example.packing.infrastructure.kafka.handler;

import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;

/**
 * Handles one (eventType, version) of the package-create-request contract.
 * Supporting a new version means adding an implementation – the processor and
 * the existing handlers stay untouched.
 */
public interface PackageCreateRequestHandler {

	String eventType();

	int version();

	void handle(PackageCreateRequestEvent event);
}
