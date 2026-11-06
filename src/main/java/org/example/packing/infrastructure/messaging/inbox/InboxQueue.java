package org.example.packing.infrastructure.messaging.inbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InboxQueue {

	void enqueue(InboxMessage message);

	List<InboxMessage> claimAvailable(int limit, Instant now, Instant leaseUntil, UUID claimToken);

	boolean deleteClaimed(UUID id, UUID claimToken);

	boolean releaseForRetry(UUID id, UUID claimToken, Instant availableAt, String lastError);
}
