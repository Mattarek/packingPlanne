package org.example.packing.application.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestItem;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.example.packing.infrastructure.kafka.exception.RetryableKafkaProcessingException;
import org.example.packing.infrastructure.persistence.repository.InboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the Inbox + validation logic of {@link PackageCreateRequestProcessor},
 * using mocked collaborators instead of Testcontainers, to complement the slower
 * end-to-end Kafka scenarios in {@code PackageCreateRequestConsumerIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
class PackageCreateRequestProcessorTest {

	@Mock
	private PackageService packageService;

	@Mock
	private InboxEventRepository inboxEventRepository;

	private Validator validator;
	private PackageCreateRequestProcessor processor;

	@BeforeEach
	void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
		processor = new PackageCreateRequestProcessor(
				packageService,
				inboxEventRepository,
				validator
		);
	}

	@Test
	void shouldRejectNullEventWithoutTouchingInboxOrPackageService() {
		assertThatThrownBy(() -> processor.process(null))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);

		verifyNoInteractions(inboxEventRepository, packageService);
	}

	@Test
	void shouldRejectEventFailingBeanValidation() {
		final PackageCreateRequestEvent event = validEvent(
				UUID.randomUUID(),
				"PACKAGE_CREATE_REQUESTED",
				1,
				List.of(new PackageCreateRequestItem(-10.0, 20.0, 30.0, 5.5))
		);

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);

		verifyNoInteractions(inboxEventRepository, packageService);
	}

	@Test
	void shouldRejectUnsupportedEventType() {
		final PackageCreateRequestEvent event = validEvent(
				UUID.randomUUID(),
				"UNKNOWN_EVENT_TYPE",
				1,
				List.of(new PackageCreateRequestItem(10.0, 20.0, 30.0, 5.5))
		);

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);

		verifyNoInteractions(inboxEventRepository, packageService);
	}

	@Test
	void shouldRejectUnsupportedEventVersion() {
		final PackageCreateRequestEvent event = validEvent(
				UUID.randomUUID(),
				"PACKAGE_CREATE_REQUESTED",
				2,
				List.of(new PackageCreateRequestItem(10.0, 20.0, 30.0, 5.5))
		);

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);

		verifyNoInteractions(inboxEventRepository, packageService);
	}

	@Test
	void shouldIgnoreDuplicateEventWithoutCreatingPackages() {
		final PackageCreateRequestEvent event = validEvent(
				UUID.randomUUID(),
				"PACKAGE_CREATE_REQUESTED",
				1,
				List.of(new PackageCreateRequestItem(10.0, 20.0, 30.0, 5.5))
		);

		when(inboxEventRepository.insertIgnoringDuplicate(
				any(), any(), anyString(), any()
		)).thenReturn(0);

		processor.process(event);

		verify(packageService, never()).createPackages(any());
	}

	@Test
	void shouldCreatePackagesForNewEvent() {
		final PackageCreateRequestEvent event = validEvent(
				UUID.randomUUID(),
				"PACKAGE_CREATE_REQUESTED",
				1,
				List.of(new PackageCreateRequestItem(10.0, 20.0, 30.0, 5.5))
		);

		when(inboxEventRepository.insertIgnoringDuplicate(
				any(), any(), anyString(), any()
		)).thenReturn(1);

		processor.process(event);

		final ArgumentCaptor<List<PackageRequest>> captor =
				ArgumentCaptor.forClass(List.class);
		verify(packageService).createPackages(captor.capture());

		assertThat(captor.getValue()).hasSize(1);
		assertThat(captor.getValue().get(0).length()).isEqualTo(10.0);
		assertThat(captor.getValue().get(0).width()).isEqualTo(20.0);
		assertThat(captor.getValue().get(0).height()).isEqualTo(30.0);
		assertThat(captor.getValue().get(0).weight()).isEqualTo(5.5);
	}

	@Test
	void shouldMapTransientErrorOnInboxInsertToRetryable() {
		final PackageCreateRequestEvent event = validEvent(
				UUID.randomUUID(),
				"PACKAGE_CREATE_REQUESTED",
				1,
				List.of(new PackageCreateRequestItem(10.0, 20.0, 30.0, 5.5))
		);

		when(inboxEventRepository.insertIgnoringDuplicate(
				any(), any(), anyString(), any()
		)).thenThrow(new TransientDataAccessResourceException("db hiccup"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(RetryableKafkaProcessingException.class);

		verifyNoInteractions(packageService);
	}

	@Test
	void shouldMapDataIntegrityViolationOnInboxInsertToNonRetryable() {
		final PackageCreateRequestEvent event = validEvent(
				UUID.randomUUID(),
				"PACKAGE_CREATE_REQUESTED",
				1,
				List.of(new PackageCreateRequestItem(10.0, 20.0, 30.0, 5.5))
		);

		when(inboxEventRepository.insertIgnoringDuplicate(
				any(), any(), anyString(), any()
		)).thenThrow(new DataIntegrityViolationException("constraint violated"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);

		verifyNoInteractions(packageService);
	}

	@Test
	void shouldMapTransientErrorOnCreatePackagesToRetryable() {
		final PackageCreateRequestEvent event = validEvent(
				UUID.randomUUID(),
				"PACKAGE_CREATE_REQUESTED",
				1,
				List.of(new PackageCreateRequestItem(10.0, 20.0, 30.0, 5.5))
		);

		when(inboxEventRepository.insertIgnoringDuplicate(
				any(), any(), anyString(), any()
		)).thenReturn(1);
		when(packageService.createPackages(any()))
				.thenThrow(new TransientDataAccessResourceException("db hiccup"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(RetryableKafkaProcessingException.class);
	}

	@Test
	void shouldMapDataIntegrityViolationOnCreatePackagesToNonRetryable() {
		final PackageCreateRequestEvent event = validEvent(
				UUID.randomUUID(),
				"PACKAGE_CREATE_REQUESTED",
				1,
				List.of(new PackageCreateRequestItem(10.0, 20.0, 30.0, 5.5))
		);

		when(inboxEventRepository.insertIgnoringDuplicate(
				any(), any(), anyString(), any()
		)).thenReturn(1);
		when(packageService.createPackages(any()))
				.thenThrow(new DataIntegrityViolationException("constraint violated"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);
	}

	private PackageCreateRequestEvent validEvent(
			final UUID eventId,
			final String eventType,
			final int version,
			final List<PackageCreateRequestItem> items
	) {
		return new PackageCreateRequestEvent(
				eventId,
				eventType,
				version,
				Instant.now(),
				items
		);
	}
}
