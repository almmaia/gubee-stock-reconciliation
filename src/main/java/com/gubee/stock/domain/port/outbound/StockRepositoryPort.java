package com.gubee.stock.domain.port.outbound;

import com.gubee.stock.domain.model.StockHistoryEntry;
import com.gubee.stock.domain.model.StockSnapshot;
import java.util.List;

public interface StockRepositoryPort {
    StockSnapshot findStock(String accountId, String sku);

    StockSnapshot getOrCreateStock(String accountId, String sku);

    StockSnapshot updateStock(String accountId, String sku, int newAvailable, long version);

    void appendHistory(String accountId, String sku, StockHistoryEntry entry);

    List<StockHistoryEntry> findHistory(String accountId, String sku);
}
