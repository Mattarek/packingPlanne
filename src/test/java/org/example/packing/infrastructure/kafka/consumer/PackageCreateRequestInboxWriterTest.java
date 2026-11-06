package org.example.packing.infrastructure.kafka.consumer;

import jakarta.validation.Validation;
import org.example.packing.application.port.in.PackageUseCase;
import org.example.packing.infrastructure.kafka.config.KafkaTopicsProperties;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestItem;
import org.example.packing.infrastructure.kafka.exception.KafkaExceptionTranslator;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.example.packing.infrastructure.kafka.exception.RetryableKafkaProcessingException;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestHandlerRegistry;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestV1Handler;
import org.example.packing.infrastructure.messaging.inbox.InboxAcceptor;
import org.example.packing.infrastructure.messaging.inbox.IncomingMessage;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageCreateRequestInboxWriterTest {

	private static final String EVENT_TYPE = "PACKAGE_CREATE_REQUESTED";
	private static final String TOPIC = "package-create-requests";
	private static final String SERIALIZED = "{\"serialized\":true}";

	@Mock
	private PackageUseCase packageUseCase;

	@Mock
	private InboxAcceptor inboxAcceptor;

	private PackageCreateRequestInboxWriter writer;

	@BeforeEach
	void setUp() {
		writer = new PackageCreateRequestInboxWriter(
				new KafkaEventValidator(Validation.buildDefaultValidatorFactory().getValidator()),
				new PackageCreateRequestHandlerRegistry(List.of(new PackageCreateRequestV1Handler(packageUseCase))),
				payload -> SERIALIZED,
				inboxAcceptor,
				new KafkaExceptionTranslator(),
				new KafkaTopicsProperties(3, TOPIC, "package-created-events")
		);
	}

	@Test
	void shouldRejectNullEventWithoutTouchingInbox() {
		assertThatThrownBy(() -> writer.write(null))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);

		verifyNoInteractions(inboxAcceptor, packageUseCase);
	}

	@Test
	void shouldRejectEventFailingBeanValidation() {
		assertThatThrownBy(() -> writer.write(event(EVENT_TYPE, 1, item(-10.0))))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);

		verifyNoInteractions(inboxAcceptor, packageUseCase);
	}

	@Test
	void shouldRejectUnsupportedEventTypeBeforeStoringIt() {
		assertThatThrownBy(() -> writer.write(event("UNKNOWN_EVENT_TYPE", 1, item(10.0))))
				.isInstanceOf(NonRetryableKafkaProcessingException.class)
				.hasMessageContaining("Unsupported event type");

		verifyNoInteractions(inboxAcceptor);
	}

	@Test
	void shouldRejectUnsupportedEventVersionBeforeStoringIt() {
		assertThatThrownBy(() -> writer.write(event(EVENT_TYPE, 2, item(10.0))))
				.isInstanceOf(NonRetryableKafkaProcessingException.class)
				.hasMessageContaining("Unsupported event version");

		verifyNoInteractions(inboxAcceptor);
	}

	@Test
	void shouldStoreEventInInboxWithoutProcessingIt() {
		final PackageCreateRequestEvent event = event(EVENT_TYPE, 1, item(10.0));
		when(inboxAcceptor.accept(any())).thenReturn(true);

		writer.write(event);

		final ArgumentCaptor<IncomingMessage> captor = ArgumentCaptor.forClass(IncomingMessage.class);
		verify(inboxAcceptor).accept(captor.capture());
		assertThat(captor.getValue()).isEqualTo(
				new IncomingMessage(event.eventId(), EVENT_TYPE, 1, TOPIC, SERIALIZED)
		);
		verifyNoInteractions(packageUseCase);
	}

	@Test
	void shouldSwallowDuplicateDelivery() {
		when(inboxAcceptor.accept(any())).thenReturn(false);

		writer.write(event(EVENT_TYPE, 1, item(10.0)));

		verifyNoInteractions(packageUseCase);
	}

	@Test
	void shouldMapTransientErrorOnInboxWriteToRetryable() {
		when(inboxAcceptor.accept(any())).thenThrow(new TransientDataAccessResourceException("db hiccup"));

		assertThatThrownBy(() -> writer.write(event(EVENT_TYPE, 1, item(10.0))))
				.isInstanceOf(RetryableKafkaProcessingException.class);
	}

	@Test
	void shouldMapDataIntegrityViolationOnInboxWriteToNonRetryable() {
		when(inboxAcceptor.accept(any())).thenThrow(new DataIntegrityViolationException("constraint violated"));

		assertThatThrownBy(() -> writer.write(event(EVENT_TYPE, 1, item(10.0))))
				.isInstanceOf(NonRetryableKafkaProcessingException.class);
	}

	private static PackageCreateRequestItem item(final double length) {
		return new PackageCreateRequestItem(length, 20.0, 30.0, 5.5, "STANDARD", "STANDARD");
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
