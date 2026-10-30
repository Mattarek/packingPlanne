package org.example.packing.infrastructure.messaging;

@FunctionalInterface
public interface PayloadSerializer {

	String serialize(Object payload);
}
