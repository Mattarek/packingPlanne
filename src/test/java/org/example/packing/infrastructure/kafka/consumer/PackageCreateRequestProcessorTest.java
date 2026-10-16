package org.example.packing.infrastructure.kafka.consumer;

import jakarta.validation.Validation;
import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.service.PackageService;
import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestItem;
import org.example.packing.infrastructure.kafka.exception.KafkaExceptionTranslator;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.example.packing.infrastructure.kafka.exception.RetryableKafkaProcessingException;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestHandler;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestHandlerRegistry;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestV1Handler;
import org.example.packing.infrastructure.kafka.inbox.InboxDeduplicator;
import org.example.packing.infrastructure.persistence.repository.InboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
 * The processor wired from its real collaborators (validator, handler registry,
 * inbox deduplicator, exception translator); only the use case and the inbox
 * table are mocked. Complements the end-to-end Kafka scenarios in
 * {@code PackageCreateRequestConsumerIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
class PackageCreateRequestProcessorTest {

	private static final String EVENT_TYPE = "PACKAGE_CREATE_REQUESTED";

	@Mock
	private PackageService packageService;

	@Mock
	private InboxEventRepository inboxEventRepository;

	private PackageCreateRequestProcessor processor;

	@BeforeEach
	void setUp() {
		processor = processorWith(List.of(new PackageCreateRequestV1Handler(packageService)));
	}

	private PackageCreateRequestProcessor processorWith(final List<PackageCreateRequestHandler> handlers) {
		return new PackageCreateRequestProcessor(
				new KafkaEventValidator(Validation.buildDefaultValidatorFactory().getValidator()),
				new PackageCreateRequestHandlerRegistry(handlers),
				new InboxDeduplicator(
						inboxEventRepository,
						UUID::randomUUID,
						Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
				),
				new KafkaExceptionTranslator()
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
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, item(-10.0, "STANDARD", "STANDARD"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);

		verifyNoInteractions(inboxEventRepository, packageService);
	}

	@Test
	void shouldRejectUnsupportedEventType() {
		final PackageCreateRequestEvent event = event("UNKNOWN_EVENT_TYPE", 1, validItem());

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class)
				.hasMessageContaining("Unsupported event type");

		verifyNoInteractions(inboxEventRepository, packageService);
	}

	@Test
	void shouldRejectUnsupportedEventVersion() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 2, validItem());

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class)
				.hasMessageContaining("Unsupported event version");

		verifyNoInteractions(inboxEventRepository, packageService);
	}

	@Test
	void shouldRouteNewVersionToItsOwnHandlerWithoutChangingTheProcessor() {
		// given: OCP – version 2 is just one more handler
		final List<PackageCreateRequestEvent> handledByV2 = new ArrayList<>();
		final PackageCreateRequestHandler v2 = new PackageCreateRequestHandler() {
			@Override
			public String eventType() {
				return EVENT_TYPE;
			}

			@Override
			public int version() {
				return 2;
			}

			@Override
			public void handle(final PackageCreateRequestEvent event) {
				handledByV2.add(event);
			}
		};
		final PackageCreateRequestProcessor withV2 = processorWith(List.of(
				new PackageCreateRequestV1Handler(packageService),
				v2
		));
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 2, validItem());

		when(inboxEventRepository.insertIgnoringDuplicate(any(), any(), anyString(), any())).thenReturn(1);

		// when
		withV2.process(event);

		// then
		assertThat(handledByV2).containsExactly(event);
		verifyNoInteractions(packageService);
	}

	@Test
	void shouldIgnoreDuplicateEventWithoutCreatingPackages() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, validItem());

		when(inboxEventRepository.insertIgnoringDuplicate(any(), any(), anyString(), any())).thenReturn(0);

		processor.process(event);

		verify(packageService, never()).createPackages(any());
	}

	@Test
	void shouldCreatePackagesForNewEvent() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, item(10.0, "FOOD", "FRAGILE"));

		when(inboxEventRepository.insertIgnoringDuplicate(any(), any(), anyString(), any())).thenReturn(1);

		processor.process(event);

		final ArgumentCaptor<List<CreatePackageCommand>> captor = ArgumentCaptor.captor();
		verify(packageService).createPackages(captor.capture());

		assertThat(captor.getValue()).containsExactly(new CreatePackageCommand(
				10.0, 20.0, 30.0, 5.5, ProductCategory.FOOD, FragilityLevel.FRAGILE
		));
	}

	@Test
	void shouldRejectUnknownWireCategoryAsNonRetryable() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, item(10.0, "SPACESHIPS", "STANDARD"));

		when(inboxEventRepository.insertIgnoringDuplicate(any(), any(), anyString(), any())).thenReturn(1);

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class)
				.hasMessageContaining("SPACESHIPS");

		verifyNoInteractions(packageService);
	}

	@Test
	void shouldMapTransientErrorOnInboxInsertToRetryable() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, validItem());

		when(inboxEventRepository.insertIgnoringDuplicate(any(), any(), anyString(), any()))
				.thenThrow(new TransientDataAccessResourceException("db hiccup"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(RetryableKafkaProcessingException.class);

		verifyNoInteractions(packageService);
	}

	@Test
	void shouldMapDataIntegrityViolationOnInboxInsertToNonRetryable() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, validItem());

		when(inboxEventRepository.insertIgnoringDuplicate(any(), any(), anyString(), any()))
				.thenThrow(new DataIntegrityViolationException("constraint violated"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);

		verifyNoInteractions(packageService);
	}

	@Test
	void shouldMapTransientErrorOnCreatePackagesToRetryable() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, validItem());

		when(inboxEventRepository.insertIgnoringDuplicate(any(), any(), anyString(), any())).thenReturn(1);
		when(packageService.createPackages(any()))
				.thenThrow(new TransientDataAccessResourceException("db hiccup"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(RetryableKafkaProcessingException.class);
	}

	@Test
	void shouldMapPackageNotAcceptedOnCreatePackagesToNonRetryable() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, validItem());

		when(inboxEventRepository.insertIgnoringDuplicate(any(), any(), anyString(), any())).thenReturn(1);
		when(packageService.createPackages(any()))
				.thenThrow(new PackageNotAcceptedException("weight too high"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);
	}

	@Test
	void shouldMapDataIntegrityViolationOnCreatePackagesToNonRetryable() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, validItem());

		when(inboxEventRepository.insertIgnoringDuplicate(any(), any(), anyString(), any())).thenReturn(1);
		when(packageService.createPackages(any()))
				.thenThrow(new DataIntegrityViolationException("constraint violated"));

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);
	}

	@Test
	void shouldFailFastWhenTwoHandlersClaimTheSameVersion() {
		final PackageCreateRequestV1Handler handler = new PackageCreateRequestV1Handler(packageService);

		assertThatThrownBy(() -> new PackageCreateRequestHandlerRegistry(List.of(handler, handler)))
				.isInstanceOf(IllegalStateException.class);
	}

	private static PackageCreateRequestItem validItem() {
		return item(10.0, "STANDARD", "STANDARD");
	}

	private static PackageCreateRequestItem item(
			final double length,
			final String category,
			final String fragility
	) {
		return new PackageCreateRequestItem(length, 20.0, 30.0, 5.5, category, fragility);
	}

	private static PackageCreateRequestEvent event(
			final String eventType,
			final int version,
			final PackageCreateRequestItem item
	) {
		return new PackageCreateRequestEvent(
				UUID.randomUUID(),
				eventType,
				version,
				Instant.parse("2026-01-01T00:00:00Z"),
				List.of(item)
		);
	}
}
