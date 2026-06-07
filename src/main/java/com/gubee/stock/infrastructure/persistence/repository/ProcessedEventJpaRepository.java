package com.gubee.stock.infrastructure.persistence.repository;

import com.gubee.stock.infrastructure.persistence.entity.ProcessedEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, String> {
    List<ProcessedEventEntity> findByStatus(String status);
}
