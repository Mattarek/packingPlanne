package org.example.packing.infrastructure.messaging;

public class PayloadDeserializationException extends RuntimeException {

	public PayloadDeserializationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
