package org.example.packing.infrastructure.kafka.deadletter;

import org.example.packing.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.example.packing.infrastructure.persistence.entity.DeadLetterEventStatus;
import org.example.packing.infrastructure.persistence.repository.DeadLetterEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadLetterRecorderTest {

	private static final Instant NOW = Instant.parse("2026-02-02T02:02:02Z");
	private static final UUID ROW_ID = UUID.fromString("00000000-0000-0000-0000-0000000000dd");

	@Mock
	private DeadLetterEventRepository repository;

	private DeadLetterRecorder recorder;

	@BeforeEach
	void setUp() {
		recorder = new DeadLetterRecorder(
				repository,
				JsonMapper.builder().build(),
				Clock.fixed(NOW, ZoneOffset.UTC),
				() -> ROW_ID,
				2
		);
	}

	@Test
	void shouldStoreNewDeadLetterWithSerializedPayload() {
		// given
		final UUID eventId = UUID.randomUUID();
		when(repository.findByEventId(eventId)).thenReturn(Optional.empty());

		// when
		recorder.record(eventId, "ANY_EVENT", "any-topic", new AnyEvent(eventId, "payload"), "boom");

		// then
		final ArgumentCaptor<DeadLetterEventEntity> captor = ArgumentCaptor.forClass(DeadLetterEventEntity.class);
		verify(repository).save(captor.capture());

		final DeadLetterEventEntity saved = captor.getValue();
		assertThat(saved.getId()).isEqualTo(ROW_ID);
		assertThat(saved.getEventId()).isEqualTo(eventId);
		assertThat(saved.getEventType()).isEqualTo("ANY_EVENT");
		assertThat(saved.getTopicName()).isEqualTo("any-topic");
		assertThat(saved.getPayload()).contains("payload");
		assertThat(saved.getReason()).isEqualTo("boom");
		assertThat(saved.getFirstFailedAt()).isEqualTo(NOW);
		assertThat(saved.getStatus()).isEqualTo(DeadLetterEventStatus.PENDING_RETRY);
	}

	@Test
	void shouldRecordAnotherFailureOnExistingRow() {
		// given
		final UUID eventId = UUID.randomUUID();
		final DeadLetterEventEntity existing = new DeadLetterEventEntity(
				UUID.randomUUID(), eventId, "ANY_EVENT", "any-topic", "{}", "first", NOW.minusSeconds(60)
		);
		when(repository.findByEventId(eventId)).thenReturn(Optional.of(existing));

		// when
		recorder.record(eventId, "ANY_EVENT", "any-topic", new AnyEvent(eventId, "payload"), "second");
		recorder.record(eventId, "ANY_EVENT", "any-topic", new AnyEvent(eventId, "payload"), "third");

		// then
		assertThat(existing.getReason()).isEqualTo("third");
		assertThat(existing.getLastFailedAt()).isEqualTo(NOW);
		assertThat(existing.getStatus()).isEqualTo(DeadLetterEventStatus.ABANDONED);
		verify(repository, never()).save(existing);
	}

	@Test
	void shouldIgnorePoisonPillWithoutEventId() {
		// when
		recorder.record(null, "ANY_EVENT", "any-topic", null, "malformed");

		// then
		verifyNoInteractions(repository);
	}

	record AnyEvent(UUID eventId, String value) {
	}
}
