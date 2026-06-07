package com.gubee.stock.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "stocks")
public class StockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int available = 0;

    @Version
    @Column(nullable = false)
    private long version = 0;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt = Instant.now();

    protected StockEntity() {
    }

    public StockEntity(String accountId, String sku, int available) {
        this.accountId = accountId;
        this.sku = sku;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getSku() {
        return sku;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public long getVersion() {
        return version;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Instant lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
