package com.gubee.stock.infrastructure.web.dto;

import com.gubee.stock.domain.model.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record StockEventRequest(
        @NotBlank String eventId,
        @NotNull EventType type,
        @NotNull Instant occurredAt,
        @NotBlank String accountId,
        @NotBlank String sku,
        String marketplace,
        String externalOrderId,
        Integer quantity,
        Integer available,
        Integer quantitySent,
        String reason
) {
}
