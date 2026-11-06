package org.example.packing.infrastructure.kafka.handler;

import org.example.packing.infrastructure.kafka.event.EventContract;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;

public interface PackageCreateRequestHandler {

	EventContract contract();

	void handle(PackageCreateRequestEvent event);
}
