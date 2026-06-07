package com.gubee.stock.domain.port.inbound;

import com.gubee.stock.domain.model.EventProcessingStatus;
import com.gubee.stock.domain.port.outbound.ProcessedEventRecord;
import java.util.List;

public interface GetEventsByStatusUseCase {
    List<ProcessedEventRecord> getByStatus(EventProcessingStatus status);
}
