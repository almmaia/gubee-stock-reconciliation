package com.gubee.stock.infrastructure.persistence.adapter;

import com.gubee.stock.domain.model.OrderState;
import com.gubee.stock.domain.port.outbound.OrderStateRepositoryPort;
import com.gubee.stock.infrastructure.persistence.entity.OrderStateEntity;
import com.gubee.stock.infrastructure.persistence.entity.OrderStateEntityId;
import com.gubee.stock.infrastructure.persistence.repository.OrderStateJpaRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OrderStateRepositoryAdapter implements OrderStateRepositoryPort {

    private final OrderStateJpaRepository repository;

    public OrderStateRepositoryAdapter(OrderStateJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderState find(String accountId, String marketplace, String externalOrderId, String sku) {
        return repository.findById(new OrderStateEntityId(accountId, marketplace, externalOrderId, sku))
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    public OrderState save(OrderState orderState) {
        OrderStateEntityId id = new OrderStateEntityId(
                orderState.accountId(),
                orderState.marketplace(),
                orderState.externalOrderId(),
                orderState.sku()
        );
        OrderStateEntity entity = repository.findById(id).orElseGet(() -> new OrderStateEntity(
                orderState.accountId(),
                orderState.marketplace(),
                orderState.externalOrderId(),
                orderState.sku()
        ));
        entity.setCreatedApplied(orderState.createdApplied());
        entity.setCancelledApplied(orderState.cancelledApplied());
        entity.setRestoredApplied(orderState.restoredApplied());
        entity.setUpdatedAt(Instant.now());
        return toDomain(repository.save(entity));
    }

    private OrderState toDomain(OrderStateEntity entity) {
        return new OrderState(
                entity.getAccountId(),
                entity.getMarketplace(),
                entity.getExternalOrderId(),
                entity.getSku(),
                entity.isCreatedApplied(),
                entity.isCancelledApplied(),
                entity.isRestoredApplied()
        );
    }
}
