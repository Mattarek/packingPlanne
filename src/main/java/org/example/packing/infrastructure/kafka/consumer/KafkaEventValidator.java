package org.example.packing.infrastructure.kafka.consumer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class KafkaEventValidator {

	private final Validator validator;

	public KafkaEventValidator(final Validator validator) {
		this.validator = validator;
	}

	public <T> void validate(final T event) {
		if (event == null) {
			throw new NonRetryableKafkaProcessingException("Event cannot be null.");
		}

		final Set<ConstraintViolation<T>> violations = validator.validate(event);

		if (!violations.isEmpty()) {
			throw new NonRetryableKafkaProcessingException(
					"Invalid event " + event.getClass().getSimpleName() + ": " + violations
			);
		}
	}
}
