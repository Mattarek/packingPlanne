package org.example.packing.infrastructure.kafka.exception;

public class RetryableKafkaProcessingException
		extends RuntimeException {

	public RetryableKafkaProcessingException(
			final String message
	) {
		super(message);
	}

	public RetryableKafkaProcessingException(
			final String message,
			final Throwable cause
	) {
		super(message, cause);
	}
}