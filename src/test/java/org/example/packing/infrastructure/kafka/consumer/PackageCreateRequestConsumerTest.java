package org.example.packing.infrastructure.kafka.consumer;

import org.example.packing.application.service.PackageCreateRequestProcessor;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.persistence.repository.DeadLetterEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PackageCreateRequestConsumerTest {

	@Mock
	private PackageCreateRequestProcessor processor;

	@Mock
	private DeadLetterEventRepository deadLetterEventRepository;

	private PackageCreateRequestConsumer consumer;

	@BeforeEach
	void setUp() {
		consumer = new PackageCreateRequestConsumer(
				processor,
				deadLetterEventRepository,
				JsonMapper.builder().build(),
				"package-create-requests",
				5
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
}
