package com.gubee.stock.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventProcessingResponse(
        String eventId,
        String status,
        String message,
        StockResponse stock
) {
}
