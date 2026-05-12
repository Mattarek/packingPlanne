package org.example.packing.domain.exception;

/**
 * Base class for all domain-specific exceptions in the packing context.
 */
public abstract class PackingException extends RuntimeException {

	protected PackingException(final String message) {
		super(message);
	}

	protected PackingException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
