package com.gubee.stock.infrastructure.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gubee.stock.domain.model.EventProcessingResult;
import com.gubee.stock.domain.model.StockEvent;
import com.gubee.stock.domain.model.StockHistoryEntry;
import com.gubee.stock.domain.model.StockSnapshot;
import com.gubee.stock.domain.port.outbound.InconsistencyRecord;
import com.gubee.stock.domain.port.outbound.ProcessedEventRecord;
import com.gubee.stock.infrastructure.web.dto.EventProcessingResponse;
import com.gubee.stock.infrastructure.web.dto.InconsistencyResponse;
import com.gubee.stock.infrastructure.web.dto.ProcessedEventResponse;
import com.gubee.stock.infrastructure.web.dto.StockEventRequest;
import com.gubee.stock.infrastructure.web.dto.StockHistoryResponse;
import com.gubee.stock.infrastructure.web.dto.StockResponse;

public final class EventMapper {

    private EventMapper() {
    }

    public static StockEvent toDomain(StockEventRequest request) {
        return new StockEvent(
                request.eventId(),
                request.type(),
                request.occurredAt(),
                request.accountId(),
                request.sku(),
                request.marketplace(),
                request.externalOrderId(),
                request.quantity(),
                request.available(),
                request.quantitySent(),
                request.reason()
        );
    }

    public static EventProcessingResponse toResponse(EventProcessingResult result) {
        return new EventProcessingResponse(
                result.eventId(),
                result.status().name(),
                result.message(),
                result.stockAfter() != null ? toStockResponse(result.stockAfter()) : null
        );
    }

    public static StockResponse toStockResponse(StockSnapshot snapshot) {
        return new StockResponse(
                snapshot.accountId(),
                snapshot.sku(),
                snapshot.available(),
                snapshot.lastUpdatedAt()
        );
    }

    public static StockHistoryResponse toHistoryResponse(StockHistoryEntry entry) {
        return new StockHistoryResponse(
                entry.eventId(),
                entry.eventType().name(),
                entry.previousQuantity(),
                entry.newQuantity(),
                entry.delta(),
                entry.description(),
                entry.occurredAt()
        );
    }

    public static ProcessedEventResponse toProcessedEventResponse(ProcessedEventRecord record) {
        return new ProcessedEventResponse(
                record.eventId(),
                record.eventType(),
                record.accountId(),
                record.sku(),
                record.status().name(),
                record.message(),
                record.occurredAt()
        );
    }

    public static InconsistencyResponse toInconsistencyResponse(InconsistencyRecord record) {
        return new InconsistencyResponse(
                record.eventId(),
                record.accountId(),
                record.sku(),
                record.reason(),
                record.occurredAt()
        );
    }

    public static String serializePayload(StockEventRequest request, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize event payload", ex);
        }
    }
}
