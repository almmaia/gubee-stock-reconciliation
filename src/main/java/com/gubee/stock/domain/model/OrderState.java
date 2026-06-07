package com.gubee.stock.domain.model;

public record OrderState(
        String accountId,
        String marketplace,
        String externalOrderId,
        String sku,
        boolean createdApplied,
        boolean cancelledApplied,
        boolean restoredApplied
) {
    public OrderState(String accountId, String marketplace, String externalOrderId, String sku) {
        this(accountId, marketplace, externalOrderId, sku, false, false, false);
    }

    public OrderState withCreatedApplied() {
        return new OrderState(accountId, marketplace, externalOrderId, sku, true, cancelledApplied, restoredApplied);
    }

    public OrderState withCancelledApplied() {
        return new OrderState(accountId, marketplace, externalOrderId, sku, createdApplied, true, restoredApplied);
    }

    public OrderState withRestoredApplied() {
        return new OrderState(accountId, marketplace, externalOrderId, sku, createdApplied, cancelledApplied, true);
    }
}
