package org.example.packing.infrastructure.kafka;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.service.PackageService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;

@Component
public class PackageCreateRequestConsumer {

	private final PackageService packageService;
	private final ObjectMapper objectMapper;
	private final Validator validator;

	public PackageCreateRequestConsumer(
			final PackageService packageService,
			final ObjectMapper objectMapper,
			final Validator validator
	) {
		this.packageService = packageService;
		this.objectMapper = objectMapper;
		this.validator = validator;
	}

	@KafkaListener(
			topics = "${app.kafka.topics.package-create-requests}",
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void consume(final String message) {
		final List<PackageRequest> requests = parseMessage(message);

		validateRequests(requests);

		final List<PackageResponse> responses = packageService.createPackages(requests);

		System.out.println("Created packages from Kafka: " + responses.size());
	}

	private List<PackageRequest> parseMessage(final String message) {
		try {
			return objectMapper.readValue(
					message,
					new TypeReference<List<PackageRequest>>() {
					}
			);
		} catch (final Exception exception) {
			throw new IllegalArgumentException("Invalid package create request Kafka message.", exception);
		}
	}

	private void validateRequests(final List<PackageRequest> requests) {
		if (requests == null || requests.isEmpty()) {
			throw new IllegalArgumentException("Package request list cannot be null or empty.");
		}

		for (final PackageRequest request : requests) {
			validateRequest(request);
		}
	}

	private void validateRequest(final PackageRequest request) {
		final Set<ConstraintViolation<PackageRequest>> violations = validator.validate(request);

		if (!violations.isEmpty()) {
			throw new IllegalArgumentException("Invalid PackageRequest: " + violations);
		}
	}
}