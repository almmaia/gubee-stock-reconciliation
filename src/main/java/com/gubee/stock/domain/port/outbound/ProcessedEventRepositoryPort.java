package com.gubee.stock.domain.port.outbound;

import com.gubee.stock.domain.model.EventProcessingStatus;
import com.gubee.stock.domain.model.StockEvent;
import java.util.List;

public interface ProcessedEventRepositoryPort {
    boolean existsByEventId(String eventId);

    void save(StockEvent event, EventProcessingStatus status, String message, String payload);

    List<ProcessedEventRecord> findByStatus(EventProcessingStatus status);
}
