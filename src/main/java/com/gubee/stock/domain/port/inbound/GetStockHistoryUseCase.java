package com.gubee.stock.domain.port.inbound;

import com.gubee.stock.domain.model.StockHistoryEntry;
import java.util.List;

public interface GetStockHistoryUseCase {
    List<StockHistoryEntry> getHistory(String accountId, String sku);
}
