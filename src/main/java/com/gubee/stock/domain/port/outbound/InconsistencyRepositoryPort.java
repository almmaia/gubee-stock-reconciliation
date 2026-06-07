package com.gubee.stock.domain.port.outbound;

import com.gubee.stock.domain.model.StockEvent;
import java.util.List;

public interface InconsistencyRepositoryPort {
    void save(StockEvent event, String reason, String payload);

    List<InconsistencyRecord> findAll();
}
