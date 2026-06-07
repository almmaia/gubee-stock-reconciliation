package com.gubee.stock.infrastructure.web.dto;

import java.time.Instant;

public record ProcessedEventResponse(
        String eventId,
        String eventType,
        String accountId,
        String sku,
        String status,
        String message,
        Instant occurredAt
) {
}
