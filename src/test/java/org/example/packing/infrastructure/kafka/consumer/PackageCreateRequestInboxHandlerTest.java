package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.port.in.PackageUseCase;
import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.kafka.event.EventContract;
import org.example.packing.infrastructure.kafka.event.EventType;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestItem;
import org.example.packing.infrastructure.kafka.exception.KafkaExceptionTranslator;
import org.example.packing.infrastructure.kafka.exception.RetryableKafkaProcessingException;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestHandler;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestHandlerRegistry;
import org.example.packing.infrastructure.kafka.handler.PackageCreateRequestV1Handler;
import org.example.packing.infrastructure.messaging.inbox.InboxMessage;
import org.example.packing.infrastructure.messaging.inbox.PermanentInboxFailureException;
import org.example.packing.infrastructure.serialization.JacksonPayloadDeserializer;
import org.example.packing.infrastructure.serialization.JacksonPayloadSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageCreateRequestInboxHandlerTest {

	private static final String EVENT_TYPE = "PACKAGE_CREATE_REQUESTED";
	private static final JsonMapper JSON = JsonMapper.builder().build();

	@Mock
	private PackageUseCase packageUseCase;

	private PackageCreateRequestInboxHandler handler;

	@BeforeEach
	void setUp() {
		handler = handlerWith(List.of(new PackageCreateRequestV1Handler(packageUseCase)));
	}

	@Test
	void shouldHandleOnlyPackageCreateRequests() {
		assertThat(handler.eventType()).isEqualTo(EVENT_TYPE);
	}

	@Test
	void shouldCreatePackagesFromStoredPayload() {
		handler.handle(message(event(1, item(10.0, "FOOD", "FRAGILE"))));

		final ArgumentCaptor<List<CreatePackageCommand>> captor = ArgumentCaptor.captor();
		verify(packageUseCase).createPackages(captor.capture());

		assertThat(captor.getValue()).containsExactly(new CreatePackageCommand(
				10.0, 20.0, 30.0, 5.5, ProductCategory.FOOD, FragilityLevel.FRAGILE
		));
	}

	@Test
	void shouldTreatUnknownWireCategoryAsPermanentFailure() {
		final InboxMessage message = message(event(1, item(10.0, "SPACESHIPS", "STANDARD")));

		assertThatThrownBy(() -> handler.handle(message))
				.isInstanceOf(PermanentInboxFailureException.class)
				.hasMessageContaining("SPACESHIPS");

		verifyNoInteractions(packageUseCase);
	}

	@Test
	void shouldTreatCorruptedPayloadAsPermanentFailure() {
		final InboxMessage message = message("{ not json");

		assertThatThrownBy(() -> handler.handle(message))
				.isInstanceOf(PermanentInboxFailureException.class);

		verifyNoInteractions(packageUseCase);
	}

	@Test
	void shouldLetTransientErrorBubbleUpAsRetryable() {
		when(packageUseCase.createPackages(any()))
				.thenThrow(new TransientDataAccessResourceException("db hiccup"));

		assertThatThrownBy(() -> handler.handle(message(event(1, validItem()))))
				.isInstanceOf(RetryableKafkaProcessingException.class);
	}

	@Test
	void shouldTreatPackageNotAcceptedAsPermanentFailure() {
		when(packageUseCase.createPackages(any()))
				.thenThrow(new PackageNotAcceptedException("weight too high"));

		assertThatThrownBy(() -> handler.handle(message(event(1, validItem()))))
				.isInstanceOf(PermanentInboxFailureException.class);
	}

	@Test
	void shouldTreatDataIntegrityViolationAsPermanentFailure() {
		when(packageUseCase.createPackages(any()))
				.thenThrow(new DataIntegrityViolationException("constraint violated"));

		assertThatThrownBy(() -> handler.handle(message(event(1, validItem()))))
				.isInstanceOf(PermanentInboxFailureException.class);
	}

	@Test
	void shouldTreatVersionWithoutHandlerAsPermanentFailure() {
		assertThatThrownBy(() -> handler.handle(message(event(2, validItem()))))
				.isInstanceOf(PermanentInboxFailureException.class)
				.hasMessageContaining("Unsupported event version");

		verifyNoInteractions(packageUseCase);
	}

	@Test
	void shouldRouteNewVersionToItsOwnHandlerWithoutChangingTheInboxHandler() {
		final List<PackageCreateRequestEvent> handledByV2 = new ArrayList<>();
		final PackageCreateRequestHandler v2 = new PackageCreateRequestHandler() {
			@Override
			public EventContract contract() {
				return new EventContract(EventType.PACKAGE_CREATE_REQUESTED, 2);
			}

			@Override
			public void handle(final PackageCreateRequestEvent event) {
				handledByV2.add(event);
			}
		};
		final PackageCreateRequestEvent event = event(2, validItem());

		handlerWith(List.of(new PackageCreateRequestV1Handler(packageUseCase), v2)).handle(message(event));

		assertThat(handledByV2).containsExactly(event);
		verifyNoInteractions(packageUseCase);
	}

	@Test
	void shouldFailFastWhenTwoHandlersClaimTheSameContract() {
		final PackageCreateRequestV1Handler v1 = new PackageCreateRequestV1Handler(packageUseCase);

		assertThatThrownBy(() -> new PackageCreateRequestHandlerRegistry(List.of(v1, v1)))
				.isInstanceOf(IllegalStateException.class);
	}

	private PackageCreateRequestInboxHandler handlerWith(final List<PackageCreateRequestHandler> handlers) {
		return new PackageCreateRequestInboxHandler(
				new JacksonPayloadDeserializer(JSON),
				new PackageCreateRequestHandlerRegistry(handlers),
				new KafkaExceptionTranslator()
		);
	}

	private static InboxMessage message(final PackageCreateRequestEvent event) {
		return message(new JacksonPayloadSerializer(JSON).serialize(event));
	}

	private static InboxMessage message(final String payload) {
		return new InboxMessage(
				UUID.randomUUID(),
				UUID.randomUUID(),
				EVENT_TYPE,
				1,
				"package-create-requests",
				payload,
				Instant.parse("2026-01-01T00:00:00Z"),
				0,
				UUID.randomUUID()
		);
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

	private static PackageCreateRequestEvent event(final int version, final PackageCreateRequestItem item) {
		return new PackageCreateRequestEvent(
				UUID.randomUUID(),
				EVENT_TYPE,
				version,
				Instant.parse("2026-01-01T00:00:00Z"),
				List.of(item)
		);
	}
}
