package com.gubee.stock.domain.port.outbound;

import com.gubee.stock.domain.model.EventProcessingStatus;
import java.time.Instant;

public record ProcessedEventRecord(
        String eventId,
        String eventType,
        String accountId,
        String sku,
        EventProcessingStatus status,
        String message,
        Instant occurredAt
) {
}
