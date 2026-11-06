package org.example.packing.infrastructure.messaging.inbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboxMessageDispatcherTest {

	@Test
	void shouldRouteMessageToHandlerOfItsEventType() {
		// given
		final RecordingHandler first = new RecordingHandler("FIRST");
		final RecordingHandler second = new RecordingHandler("SECOND");
		final InboxMessage message = message("SECOND");

		// when
		new InboxMessageDispatcher(List.of(first, second)).dispatch(message);

		// then
		assertThat(first.handled).isEmpty();
		assertThat(second.handled).containsExactly(message);
	}

	@Test
	void shouldTreatUnknownEventTypeAsPermanentFailure() {
		final InboxMessageDispatcher dispatcher = new InboxMessageDispatcher(List.of(new RecordingHandler("FIRST")));

		assertThatThrownBy(() -> dispatcher.dispatch(message("OTHER")))
				.isInstanceOf(PermanentInboxFailureException.class)
				.hasMessageContaining("OTHER");
	}

	@Test
	void shouldFailFastWhenTwoHandlersClaimTheSameEventType() {
		assertThatThrownBy(() -> new InboxMessageDispatcher(List.of(
				new RecordingHandler("SAME"), new RecordingHandler("SAME")
		))).isInstanceOf(IllegalStateException.class);
	}

	private static InboxMessage message(final String eventType) {
		return new InboxMessage(
				UUID.randomUUID(), UUID.randomUUID(), eventType, 1, "topic", "{}", Instant.EPOCH, 0, UUID.randomUUID()
		);
	}

	private static final class RecordingHandler implements InboxMessageHandler {

		private final String eventType;
		private final List<InboxMessage> handled = new ArrayList<>();

		private RecordingHandler(final String eventType) {
			this.eventType = eventType;
		}

		@Override
		public String eventType() {
			return eventType;
		}

		@Override
		public void handle(final InboxMessage message) {
			handled.add(message);
		}
	}
}
