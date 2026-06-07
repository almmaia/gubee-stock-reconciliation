package com.gubee.stock.domain.port.inbound;

import com.gubee.stock.domain.model.StockSnapshot;

public interface GetStockUseCase {
    StockSnapshot getStock(String accountId, String sku);
}
