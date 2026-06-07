package com.gubee.stock.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class OrderStateEntityId implements Serializable {

    private String accountId;
    private String marketplace;
    private String externalOrderId;
    private String sku;

    public OrderStateEntityId() {
    }

    public OrderStateEntityId(String accountId, String marketplace, String externalOrderId, String sku) {
        this.accountId = accountId;
        this.marketplace = marketplace;
        this.externalOrderId = externalOrderId;
        this.sku = sku;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderStateEntityId that)) {
            return false;
        }
        return Objects.equals(accountId, that.accountId)
                && Objects.equals(marketplace, that.marketplace)
                && Objects.equals(externalOrderId, that.externalOrderId)
                && Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, marketplace, externalOrderId, sku);
    }
}
