package com.gubee.stock.infrastructure.persistence.adapter;

import com.gubee.stock.domain.model.EventType;
import com.gubee.stock.domain.model.StockHistoryEntry;
import com.gubee.stock.domain.model.StockSnapshot;
import com.gubee.stock.domain.port.outbound.StockRepositoryPort;
import com.gubee.stock.infrastructure.persistence.entity.StockEntity;
import com.gubee.stock.infrastructure.persistence.entity.StockHistoryEntity;
import com.gubee.stock.infrastructure.persistence.repository.StockHistoryJpaRepository;
import com.gubee.stock.infrastructure.persistence.repository.StockJpaRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StockRepositoryAdapter implements StockRepositoryPort {

    private final StockJpaRepository stockJpaRepository;
    private final StockHistoryJpaRepository stockHistoryJpaRepository;

    public StockRepositoryAdapter(
            StockJpaRepository stockJpaRepository,
            StockHistoryJpaRepository stockHistoryJpaRepository
    ) {
        this.stockJpaRepository = stockJpaRepository;
        this.stockHistoryJpaRepository = stockHistoryJpaRepository;
    }

    @Override
    public StockSnapshot findStock(String accountId, String sku) {
        return stockJpaRepository.findByAccountIdAndSku(accountId, sku)
                .map(this::toSnapshot)
                .orElse(null);
    }

    @Override
    public StockSnapshot getOrCreateStock(String accountId, String sku) {
        return stockJpaRepository.findByAccountIdAndSku(accountId, sku)
                .map(this::toSnapshot)
                .orElseGet(() -> toSnapshot(stockJpaRepository.save(new StockEntity(accountId, sku, 0))));
    }

    @Override
    public StockSnapshot updateStock(String accountId, String sku, int newAvailable, long version) {
        if (newAvailable < 0) {
            throw new IllegalArgumentException("Stock cannot be negative: " + newAvailable);
        }
        StockEntity entity = stockJpaRepository.findByAccountIdAndSku(accountId, sku)
                .orElseThrow(() -> new IllegalStateException("Stock not found for " + accountId + "/" + sku));
        if (entity.getVersion() != version) {
            throw new IllegalStateException(
                    "Stale stock version for " + accountId + "/" + sku + " (expected " + version + ", found " + entity.getVersion() + ")"
            );
        }
        entity.setAvailable(newAvailable);
        entity.setLastUpdatedAt(Instant.now());
        return toSnapshot(stockJpaRepository.save(entity));
    }

    @Override
    public void appendHistory(String accountId, String sku, StockHistoryEntry entry) {
        stockHistoryJpaRepository.save(new StockHistoryEntity(
                accountId,
                sku,
                entry.eventId(),
                entry.eventType().name(),
                entry.previousQuantity(),
                entry.newQuantity(),
                entry.delta(),
                entry.description(),
                entry.occurredAt()
        ));
    }

    @Override
    public List<StockHistoryEntry> findHistory(String accountId, String sku) {
        return stockHistoryJpaRepository.findByAccountIdAndSkuOrderByOccurredAtAsc(accountId, sku)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private StockSnapshot toSnapshot(StockEntity entity) {
        return new StockSnapshot(
                entity.getAccountId(),
                entity.getSku(),
                entity.getAvailable(),
                entity.getVersion(),
                entity.getLastUpdatedAt()
        );
    }

    private StockHistoryEntry toDomain(StockHistoryEntity entity) {
        return new StockHistoryEntry(
                entity.getEventId(),
                EventType.valueOf(entity.getEventType()),
                entity.getPreviousQuantity(),
                entity.getNewQuantity(),
                entity.getDelta(),
                entity.getDescription(),
                entity.getOccurredAt()
        );
    }
}
