package org.example.packing.infrastructure.messaging;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface MessageSender {

	CompletableFuture<Void> send(String topic, String key, String payload);
}
