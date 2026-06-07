package com.gubee.stock.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "order_states")
@IdClass(OrderStateEntityId.class)
public class OrderStateEntity {

    @Id
    @Column(name = "account_id")
    private String accountId;

    @Id
    @Column
    private String marketplace;

    @Id
    @Column(name = "external_order_id")
    private String externalOrderId;

    @Id
    @Column
    private String sku;

    @Column(name = "created_applied", nullable = false)
    private boolean createdApplied = false;

    @Column(name = "cancelled_applied", nullable = false)
    private boolean cancelledApplied = false;

    @Column(name = "restored_applied", nullable = false)
    private boolean restoredApplied = false;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected OrderStateEntity() {
    }

    public OrderStateEntity(String accountId, String marketplace, String externalOrderId, String sku) {
        this.accountId = accountId;
        this.marketplace = marketplace;
        this.externalOrderId = externalOrderId;
        this.sku = sku;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public String getSku() {
        return sku;
    }

    public boolean isCreatedApplied() {
        return createdApplied;
    }

    public void setCreatedApplied(boolean createdApplied) {
        this.createdApplied = createdApplied;
    }

    public boolean isCancelledApplied() {
        return cancelledApplied;
    }

    public void setCancelledApplied(boolean cancelledApplied) {
        this.cancelledApplied = cancelledApplied;
    }

    public boolean isRestoredApplied() {
        return restoredApplied;
    }

    public void setRestoredApplied(boolean restoredApplied) {
        this.restoredApplied = restoredApplied;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
