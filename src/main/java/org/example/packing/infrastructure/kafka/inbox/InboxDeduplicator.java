package org.example.packing.infrastructure.kafka.inbox;

import org.example.packing.application.port.IdGenerator;
import org.example.packing.infrastructure.persistence.repository.InboxEventRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

/**
 * Idempotent-consumer guard backed by the {@code inbox_events} table. Must be
 * called inside the transaction that processes the event, so the inbox row
 * and the business changes commit (or roll back) together.
 */
@Component
public class InboxDeduplicator {

	private final InboxEventRepository inboxEventRepository;
	private final IdGenerator idGenerator;
	private final Clock clock;

	public InboxDeduplicator(
			final InboxEventRepository inboxEventRepository,
			final IdGenerator idGenerator,
			final Clock clock
	) {
		this.inboxEventRepository = inboxEventRepository;
		this.idGenerator = idGenerator;
		this.clock = clock;
	}

	/**
	 * @return {@code true} on the first delivery of {@code eventId},
	 * {@code false} when the event was already processed
	 */
	public boolean registerFirstDelivery(final UUID eventId, final String eventType) {
		final int insertedRows = inboxEventRepository.insertIgnoringDuplicate(
				idGenerator.newId(),
				eventId,
				eventType,
				clock.instant()
		);

		return insertedRows > 0;
	}
}
