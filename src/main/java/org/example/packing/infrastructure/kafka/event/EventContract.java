package org.example.packing.infrastructure.kafka.event;

import java.util.Objects;

public record EventContract(EventType type, int version) {

	public EventContract {
		Objects.requireNonNull(type, "type must not be null");
		if (version < 1) {
			throw new IllegalArgumentException("version must be positive: " + version);
		}
	}

	public static EventContract v1(final EventType type) {
		return new EventContract(type, 1);
	}

	public boolean hasType(final String wireValue) {
		return type.wireValue().equals(wireValue);
	}
}
