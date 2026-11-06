package org.example.packing.domain.exception;

public abstract class BusinessRuleViolationException extends PackingException {

	protected BusinessRuleViolationException(final String message) {
		super(message);
	}
}
