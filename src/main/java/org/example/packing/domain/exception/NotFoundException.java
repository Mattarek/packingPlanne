package org.example.packing.domain.exception;

public abstract class NotFoundException extends PackingException {

	protected NotFoundException(final String message) {
		super(message);
	}
}
