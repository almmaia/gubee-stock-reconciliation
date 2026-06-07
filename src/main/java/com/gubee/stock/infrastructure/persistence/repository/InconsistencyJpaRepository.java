package com.gubee.stock.infrastructure.persistence.repository;

import com.gubee.stock.infrastructure.persistence.entity.InconsistencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InconsistencyJpaRepository extends JpaRepository<InconsistencyEntity, Long> {
}
