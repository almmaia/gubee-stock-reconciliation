package com.gubee.stock.domain.model;

import java.time.Instant;

public record StockSnapshot(
        String accountId,
        String sku,
        int available,
        long version,
        Instant lastUpdatedAt
) {
}
