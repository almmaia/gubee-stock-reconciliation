package com.gubee.stock.infrastructure.web.dto;

import java.time.Instant;

public record InconsistencyResponse(
        String eventId,
        String accountId,
        String sku,
        String reason,
        Instant occurredAt
) {
}
