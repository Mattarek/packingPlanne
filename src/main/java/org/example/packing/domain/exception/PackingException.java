package org.example.packing.domain.exception;

public abstract class PackingException extends RuntimeException {

	protected PackingException(final String message) {
		super(message);
	}

	protected PackingException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
