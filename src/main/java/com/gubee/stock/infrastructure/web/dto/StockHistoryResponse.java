package com.gubee.stock.infrastructure.web.dto;

import java.time.Instant;

public record StockHistoryResponse(
        String eventId,
        String eventType,
        int previousQuantity,
        int newQuantity,
        int delta,
        String description,
        Instant occurredAt
) {
}
