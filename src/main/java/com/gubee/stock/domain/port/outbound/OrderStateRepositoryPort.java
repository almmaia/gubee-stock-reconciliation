package com.gubee.stock.domain.port.outbound;

import com.gubee.stock.domain.model.OrderState;

public interface OrderStateRepositoryPort {
    OrderState find(String accountId, String marketplace, String externalOrderId, String sku);

    OrderState save(OrderState orderState);
}
