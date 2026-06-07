package com.gubee.stock.domain.port.outbound;

import java.time.Instant;

public record InconsistencyRecord(
        String eventId,
        String accountId,
        String sku,
        String reason,
        Instant occurredAt
) {
}
