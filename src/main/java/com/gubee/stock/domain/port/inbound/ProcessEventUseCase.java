package com.gubee.stock.domain.port.inbound;

import com.gubee.stock.domain.model.EventProcessingResult;
import com.gubee.stock.domain.model.StockEvent;

public interface ProcessEventUseCase {
    EventProcessingResult process(StockEvent event, String payload);
}
