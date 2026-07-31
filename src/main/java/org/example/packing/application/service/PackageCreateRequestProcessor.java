package org.example.packing.application.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestItem;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.example.packing.infrastructure.kafka.exception.RetryableKafkaProcessingException;
import org.example.packing.infrastructure.persistence.repository.InboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PackageCreateRequestProcessor {
	private static final Logger log =
			LoggerFactory.getLogger(
					PackageCreateRequestProcessor.class
			);

	private static final String SUPPORTED_EVENT_TYPE =
			"PACKAGE_CREATE_REQUESTED";

	private static final int SUPPORTED_VERSION = 1;

	private final PackageService packageService;
	private final InboxEventRepository inboxEventRepository;
	private final Validator validator;

	public PackageCreateRequestProcessor(
			final PackageService packageService,
			final InboxEventRepository inboxEventRepository,
			final Validator validator
	) {
		this.packageService = packageService;
		this.inboxEventRepository = inboxEventRepository;
		this.validator = validator;
	}

	@Transactional
	public void process(
			final PackageCreateRequestEvent event
	) {
		validateEvent(event);

		final int insertedRows;
		try {
			insertedRows = inboxEventRepository.insertIgnoringDuplicate(
					UUID.randomUUID(),
					event.eventId(),
					event.eventType(),
					Instant.now()
			);
		} catch (final TransientDataAccessException exception) {
			throw new RetryableKafkaProcessingException(
					"Transient database error while recording inbox event: eventId="
							+ event.eventId(),
					exception
			);
		} catch (final DataIntegrityViolationException exception) {
			throw new NonRetryableKafkaProcessingException(
					"Data integrity violation while recording inbox event: eventId="
							+ event.eventId(),
					exception
			);
		}

		if (insertedRows == 0) {
			log.info(
					"Ignoring duplicated Kafka event: eventId={}",
					event.eventId()
			);

			return;
		}

		final List<PackageResponse> responses;
		try {
			responses = packageService.createPackages(
					toPackageRequests(event.packages())
			);
		} catch (final TransientDataAccessException exception) {
			throw new RetryableKafkaProcessingException(
					"Transient database error while creating packages: eventId="
							+ event.eventId(),
					exception
			);
		} catch (final DataIntegrityViolationException exception) {
			throw new NonRetryableKafkaProcessingException(
					"Data integrity violation while creating packages: eventId="
							+ event.eventId(),
					exception
			);
		}

		log.info(
				"Package create event processed: eventId={}, createdPackages={}",
				event.eventId(),
				responses.size()
		);
	}

	private List<PackageRequest> toPackageRequests(
			final List<PackageCreateRequestItem> items
	) {
		return items.stream()
				.map(item -> new PackageRequest(
						item.length(),
						item.width(),
						item.height(),
						item.weight()
				))
				.toList();
	}

	private void validateEvent(
			final PackageCreateRequestEvent event
	) {
		if (event == null) {
			throw new NonRetryableKafkaProcessingException(
					"Package create event cannot be null."
			);
		}

		final Set<ConstraintViolation<PackageCreateRequestEvent>>
				violations = validator.validate(event);

		if (!violations.isEmpty()) {
			throw new NonRetryableKafkaProcessingException(
					"Invalid package create event: " + violations
			);
		}

		if (!SUPPORTED_EVENT_TYPE.equals(event.eventType())) {
			throw new NonRetryableKafkaProcessingException(
					"Unsupported event type: "
							+ event.eventType()
			);
		}

		if (event.version() != SUPPORTED_VERSION) {
			throw new NonRetryableKafkaProcessingException(
					"Unsupported event version: "
							+ event.version()
			);
		}
	}

	//@TODO rozmiary paczek, czy mieszcza sie w pojezdzie
	//@TODO zla waga
	//@TODO polityka firmy, ktorych produktow nie przewozimy
	//@TODO walidator do paczek i serializacji
	//@TODO happypath - logowanie, ze cos zostalo przyjete
	//@TODO jak generycznie stworzyc producera
}