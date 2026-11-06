package org.example.packing.infrastructure.messaging.deadletter;

import org.example.packing.infrastructure.messaging.PayloadSerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadLetterRecorderTest {

	private static final Instant NOW = Instant.parse("2026-02-02T02:02:02Z");
	private static final UUID ROW_ID = UUID.fromString("00000000-0000-0000-0000-0000000000dd");

	@Mock
	private DeadLetterStore store;

	private DeadLetterRecorder recorder;

	@BeforeEach
	void setUp() {
		recorder = new DeadLetterRecorder(
				store,
				payload -> "json:" + payload,
				Clock.fixed(NOW, ZoneOffset.UTC),
				() -> ROW_ID,
				new DeadLetterProperties(50, 2)
		);
	}

	@Test
	void shouldStoreFirstFailureWithSerializedPayload() {
		// given
		final UUID eventId = UUID.randomUUID();
		when(store.findByEventId(eventId)).thenReturn(Optional.empty());

		// when
		recorder.record(eventId, "ANY_EVENT", "any-topic", "event-body", "boom");

		// then
		final ArgumentCaptor<DeadLetter> captor = ArgumentCaptor.forClass(DeadLetter.class);
		verify(store).save(captor.capture());

		final DeadLetter saved = captor.getValue();
		assertThat(saved.getId()).isEqualTo(ROW_ID);
		assertThat(saved.getEventId()).isEqualTo(eventId);
		assertThat(saved.getEventType()).isEqualTo("ANY_EVENT");
		assertThat(saved.getTopicName()).isEqualTo("any-topic");
		assertThat(saved.getPayload()).isEqualTo("json:event-body");
		assertThat(saved.getReason()).isEqualTo("boom");
		assertThat(saved.getFirstFailedAt()).isEqualTo(NOW);
		assertThat(saved.getStatus()).isEqualTo(DeadLetterEventStatus.PENDING_RETRY);
	}

	@Test
	void shouldRecordAnotherFailureOnExistingDeadLetterAndSaveIt() {
		// given
		final UUID eventId = UUID.randomUUID();
		final DeadLetter existing = DeadLetter.firstFailure(
				UUID.randomUUID(), eventId, "ANY_EVENT", "any-topic", "{}", "first", NOW.minusSeconds(60)
		);
		when(store.findByEventId(eventId)).thenReturn(Optional.of(existing));

		// when
		recorder.record(eventId, "ANY_EVENT", "any-topic", "event-body", "second");
		recorder.record(eventId, "ANY_EVENT", "any-topic", "event-body", "third");

		// then
		assertThat(existing.getReason()).isEqualTo("third");
		assertThat(existing.getLastFailedAt()).isEqualTo(NOW);
		assertThat(existing.getStatus()).isEqualTo(DeadLetterEventStatus.ABANDONED);
		verify(store, times(2)).save(existing);
	}

	@Test
	void shouldNotStoreAnythingWhenPayloadCannotBeSerialized() {
		// given
		final DeadLetterRecorder failingSerializer = new DeadLetterRecorder(
				store,
				payload -> {
					throw new PayloadSerializationException("boom", new RuntimeException());
				},
				Clock.fixed(NOW, ZoneOffset.UTC),
				() -> ROW_ID,
				new DeadLetterProperties(50, 2)
		);
		final UUID eventId = UUID.randomUUID();

		// when
		failingSerializer.record(eventId, "ANY_EVENT", "any-topic", "event-body", "boom");

		// then
		verifyNoInteractions(store);
	}

	@Test
	void shouldIgnorePoisonPillWithoutEventId() {
		// when
		recorder.record(null, "ANY_EVENT", "any-topic", null, "malformed");

		// then
		verifyNoInteractions(store);
	}
}
