package com.gubee.stock.domain.model;

public record EventProcessingResult(
        String eventId,
        EventProcessingStatus status,
        String message,
        StockSnapshot stockAfter
) {
    public EventProcessingResult(String eventId, EventProcessingStatus status, String message) {
        this(eventId, status, message, null);
    }
}
