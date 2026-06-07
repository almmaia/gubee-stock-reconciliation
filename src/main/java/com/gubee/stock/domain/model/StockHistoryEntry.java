package com.gubee.stock.domain.model;

import java.time.Instant;

public record StockHistoryEntry(
        String eventId,
        EventType eventType,
        int previousQuantity,
        int newQuantity,
        int delta,
        String description,
        Instant occurredAt
) {
}
