package org.example.packing.infrastructure.messaging.inbox;

public class PermanentInboxFailureException extends RuntimeException {

	public PermanentInboxFailureException(final String message) {
		super(message);
	}

	public PermanentInboxFailureException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
