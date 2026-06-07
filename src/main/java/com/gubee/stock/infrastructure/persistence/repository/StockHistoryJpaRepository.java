package com.gubee.stock.infrastructure.persistence.repository;

import com.gubee.stock.infrastructure.persistence.entity.StockHistoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockHistoryJpaRepository extends JpaRepository<StockHistoryEntity, Long> {
    List<StockHistoryEntity> findByAccountIdAndSkuOrderByOccurredAtAsc(String accountId, String sku);
}
