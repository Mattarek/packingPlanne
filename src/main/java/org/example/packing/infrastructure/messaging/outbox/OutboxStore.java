package org.example.packing.infrastructure.messaging.outbox;

import java.time.Instant;
import java.util.List;

public interface OutboxStore {

	void add(OutboxMessage message);

	List<OutboxMessage> lockNextPendingSkippingLocked(int limit);

	void saveAll(List<OutboxMessage> messages);

	int deletePublishedBefore(Instant publishedBefore);
}
