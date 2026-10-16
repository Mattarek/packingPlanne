package org.example.packing.infrastructure.kafka.exception;

import org.example.packing.domain.exception.PackingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Decides whether a failure while processing a Kafka event is worth retrying.
 * This is a transport concern, so it lives next to the consumer – the use
 * cases just throw their normal exceptions.
 */
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
			// Every domain exception means "this input is wrong" – retrying won't help.
			throw new NonRetryableKafkaProcessingException(
					"Event rejected by domain rules: eventId=" + eventId,
					exception
			);
		}
	}
}
