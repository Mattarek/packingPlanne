package org.example.packing.domain.exception;

/**
 * Base of every domain exception, so {@code catch (PackingException e)} really
 * catches all of them. Concrete exceptions extend one of the semantic
 * subtypes ({@link NotFoundException}, {@link BusinessRuleViolationException}),
 * which is what the outer layers react to – never the concrete class.
 */
public abstract class PackingException extends RuntimeException {

	protected PackingException(final String message) {
		super(message);
	}

	protected PackingException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
