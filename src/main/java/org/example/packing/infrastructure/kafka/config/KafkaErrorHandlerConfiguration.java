package org.example.packing.infrastructure.kafka.config;

import org.apache.kafka.common.TopicPartition;
import org.example.packing.domain.exception.PackageTooLargeException;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.example.packing.infrastructure.kafka.exception.RetryableKafkaProcessingException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfiguration {

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(
			final KafkaTemplate<String, String> kafkaTemplate
	) {
		final DeadLetterPublishingRecoverer recoverer =
				new DeadLetterPublishingRecoverer(
						kafkaTemplate,
						(record, _) ->
								new TopicPartition(
										record.topic() + ".DLT",
										record.partition()
								)
				);

		/*
		 * 1000 ms przerwy.
		 * 2 ponowienia.
		 *
		 * Łącznie:
		 * - pierwsza próba,
		 * - retry 1,
		 * - retry 2.
		 * pierwsza próba
				↓ błąd
			czekaj 1 sekundę
				↓
			retry 1
				↓ błąd
			czekaj 1 sekundę
				↓
			retry 2
				↓ błąd
			DLT
		 *
		 */
		final FixedBackOff backOff =
				new FixedBackOff(1_000L, 2L); //1000ms, 2x retry

		final DefaultErrorHandler errorHandler =
				new DefaultErrorHandler(
						recoverer,
						backOff
				);

		errorHandler.addRetryableExceptions(
				RetryableKafkaProcessingException.class
		);

		errorHandler.addNotRetryableExceptions(
				NonRetryableKafkaProcessingException.class,
				PackageTooLargeException.class
		);

		return errorHandler;
	}
}