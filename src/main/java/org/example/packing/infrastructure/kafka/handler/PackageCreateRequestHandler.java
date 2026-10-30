package org.example.packing.infrastructure.kafka.handler;

import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;

public interface PackageCreateRequestHandler {

	String eventType();

	int version();

	void handle(PackageCreateRequestEvent event);
}
