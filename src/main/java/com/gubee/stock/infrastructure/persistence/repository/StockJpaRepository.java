package com.gubee.stock.infrastructure.persistence.repository;

import com.gubee.stock.infrastructure.persistence.entity.StockEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockJpaRepository extends JpaRepository<StockEntity, Long> {
    Optional<StockEntity> findByAccountIdAndSku(String accountId, String sku);
}
