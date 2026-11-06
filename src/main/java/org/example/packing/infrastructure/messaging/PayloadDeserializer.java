package org.example.packing.infrastructure.messaging;

@FunctionalInterface
public interface PayloadDeserializer {

	<T> T deserialize(String payload, Class<T> type);
}
