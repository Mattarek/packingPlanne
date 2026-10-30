package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.infrastructure.kafka.config.KafkaTopicsProperties;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.messaging.deadletter.DeadLetterRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageCreateRequestConsumerTest {

	@Mock
	private PackageCreateRequestProcessor processor;

	@Mock
	private DeadLetterRecorder deadLetterRecorder;

	private PackageCreateRequestConsumer consumer;

	@BeforeEach
	void setUp() {
		consumer = new PackageCreateRequestConsumer(
				processor,
				deadLetterRecorder,
				new KafkaTopicsProperties(3, "package-create-requests", "package-created-events")
		);
	}

	@Test
	void shouldDelegateConsumedEventToProcessor() {
		// given
		final PackageCreateRequestEvent event = mock(PackageCreateRequestEvent.class);

		// when
		consumer.consume(event);

		// then
		verify(processor).process(event);
		verifyNoMoreInteractions(processor);
	}

	@Test
	void shouldDelegateDeadLetterToRecorder() {
		// given
		final UUID eventId = UUID.randomUUID();
		final PackageCreateRequestEvent event = mock(PackageCreateRequestEvent.class);
		when(event.eventId()).thenReturn(eventId);

		// when
		consumer.handleDlt(event, "boom");

		// then
		verify(deadLetterRecorder).record(
				eventId, "PACKAGE_CREATE_REQUESTED", "package-create-requests", event, "boom"
		);
	}

	@Test
	void shouldPassNullIdToRecorderForPoisonPill() {
		// when
		consumer.handleDlt(null, "malformed");

		// then
		verify(deadLetterRecorder).record(isNull(), any(), any(), isNull(), any());
	}
}
