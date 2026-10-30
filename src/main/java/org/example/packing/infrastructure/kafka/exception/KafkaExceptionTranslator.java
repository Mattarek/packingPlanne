package org.example.packing.infrastructure.kafka.exception;

import org.example.packing.domain.exception.PackingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KafkaExceptionTranslator {

	public void runTranslatingExceptions(final UUID eventId, final Runnable action) {
		try {
			action.run();
		} catch (final TransientDataAccessException exception) {
			throw new RetryableKafkaProcessingException(
					"Transient database error while processing event: eventId=" + eventId,
					exception
			);
		} catch (final DataIntegrityViolationException exception) {
			throw new NonRetryableKafkaProcessingException(
					"Data integrity violation while processing event: eventId=" + eventId,
					exception
			);
		} catch (final PackingException exception) {
			throw new NonRetryableKafkaProcessingException(
					"Event rejected by domain rules: eventId=" + eventId,
					exception
			);
		}
	}
}
