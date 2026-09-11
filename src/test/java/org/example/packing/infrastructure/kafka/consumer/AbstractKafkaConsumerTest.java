package org.example.packing.infrastructure.kafka.consumer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractKafkaConsumerTest {

	@Test
	void shouldDelegateHandledEventToProcess() {
		// given
		final RecordingConsumer consumer = new RecordingConsumer();

		// when
		consumer.handle("some-event");

		// then
		assertThat(consumer.processedEvents).containsExactly("some-event");
	}

	@Test
	void shouldPropagateExceptionThrownByProcessUnchanged() {
		// given
		final FailingConsumer consumer = new FailingConsumer();

		// when & then
		assertThatThrownBy(() -> consumer.handle("some-event"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("boom");
	}

	private static final class RecordingConsumer extends AbstractKafkaConsumer<String> {

		private final List<String> processedEvents = new ArrayList<>();

		@Override
		protected void process(final String event) {
			processedEvents.add(event);
		}
	}

	private static final class FailingConsumer extends AbstractKafkaConsumer<String> {

		@Override
		protected void process(final String event) {
			throw new IllegalStateException("boom");
		}
	}
}
