package org.example.packing.infrastructure.kafka.handler;

import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PackageCreateRequestHandlerRegistry {

	private final Map<HandlerKey, PackageCreateRequestHandler> handlers;

	public PackageCreateRequestHandlerRegistry(final List<PackageCreateRequestHandler> handlers) {
		this.handlers = handlers.stream()
				.collect(Collectors.toUnmodifiableMap(
						handler -> new HandlerKey(handler.eventType(), handler.version()),
						Function.identity()
				));
	}

	public PackageCreateRequestHandler handlerFor(final PackageCreateRequestEvent event) {
		final PackageCreateRequestHandler handler =
				handlers.get(new HandlerKey(event.eventType(), event.version()));

		if (handler != null) {
			return handler;
		}

		final boolean typeKnown = handlers.keySet().stream()
				.anyMatch(key -> key.eventType().equals(event.eventType()));

		throw new NonRetryableKafkaProcessingException(
				typeKnown
						? "Unsupported event version: " + event.version()
						: "Unsupported event type: " + event.eventType()
		);
	}

	private record HandlerKey(String eventType, int version) {
	}
}
