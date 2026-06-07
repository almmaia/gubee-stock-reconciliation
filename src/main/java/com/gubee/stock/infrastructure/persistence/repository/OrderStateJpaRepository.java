package com.gubee.stock.infrastructure.persistence.repository;

import com.gubee.stock.infrastructure.persistence.entity.OrderStateEntity;
import com.gubee.stock.infrastructure.persistence.entity.OrderStateEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStateJpaRepository extends JpaRepository<OrderStateEntity, OrderStateEntityId> {
}
