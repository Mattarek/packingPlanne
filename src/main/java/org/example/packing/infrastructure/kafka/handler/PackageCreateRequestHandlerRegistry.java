package org.example.packing.infrastructure.kafka.handler;

import org.example.packing.infrastructure.kafka.event.EventContract;
import org.example.packing.infrastructure.kafka.event.EventType;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PackageCreateRequestHandlerRegistry {

	private final Map<EventContract, PackageCreateRequestHandler> handlers;

	public PackageCreateRequestHandlerRegistry(final List<PackageCreateRequestHandler> handlers) {
		this.handlers = handlers.stream()
				.collect(Collectors.toUnmodifiableMap(PackageCreateRequestHandler::contract, Function.identity()));
	}

	public PackageCreateRequestHandler handlerFor(final PackageCreateRequestEvent event) {
		return EventType.findByWireValue(event.eventType())
				.map(type -> new EventContract(type, event.version()))
				.map(handlers::get)
				.orElseThrow(() -> unsupported(event));
	}

	private NonRetryableKafkaProcessingException unsupported(final PackageCreateRequestEvent event) {
		final boolean typeHandled = handlers.keySet().stream()
				.anyMatch(contract -> contract.hasType(event.eventType()));

		return new NonRetryableKafkaProcessingException(
				typeHandled
						? "Unsupported event version: " + event.version()
						: "Unsupported event type: " + event.eventType()
		);
	}
}
