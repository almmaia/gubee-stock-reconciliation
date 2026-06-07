package com.gubee.stock.domain.model;

import java.time.Instant;

public record StockEvent(
        String eventId,
        EventType type,
        Instant occurredAt,
        String accountId,
        String sku,
        String marketplace,
        String externalOrderId,
        Integer quantity,
        Integer available,
        Integer quantitySent,
        String reason
) {
}
