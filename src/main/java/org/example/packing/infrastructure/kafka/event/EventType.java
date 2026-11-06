package org.example.packing.infrastructure.kafka.event;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum EventType {
	PACKAGE_CREATE_REQUESTED("PACKAGE_CREATE_REQUESTED"),
	PACKAGE_CREATED("PACKAGE_CREATED");

	private static final Map<String, EventType> BY_WIRE_VALUE = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(EventType::wireValue, Function.identity()));

	private final String wireValue;

	EventType(final String wireValue) {
		this.wireValue = wireValue;
	}

	public String wireValue() {
		return wireValue;
	}

	public static Optional<EventType> findByWireValue(final String wireValue) {
		return Optional.ofNullable(wireValue).map(BY_WIRE_VALUE::get);
	}
}
