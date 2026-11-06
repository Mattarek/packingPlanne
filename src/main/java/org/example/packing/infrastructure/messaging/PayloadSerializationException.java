package org.example.packing.infrastructure.messaging;

public class PayloadSerializationException extends RuntimeException {

	public PayloadSerializationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
