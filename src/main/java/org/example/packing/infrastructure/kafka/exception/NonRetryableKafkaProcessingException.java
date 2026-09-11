package org.example.packing.infrastructure.kafka.exception;

public class NonRetryableKafkaProcessingException extends RuntimeException {

	public NonRetryableKafkaProcessingException(final String message) {
		super(message);
	}

	public NonRetryableKafkaProcessingException(
			final String message,
			final Throwable cause
	) {
		super(message, cause);
	}
}