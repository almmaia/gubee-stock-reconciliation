package com.gubee.stock.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "stock_history")
public class StockHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String sku;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "previous_quantity", nullable = false)
    private int previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private int newQuantity;

    @Column(nullable = false)
    private int delta;

    @Column(nullable = false)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected StockHistoryEntity() {
    }

    public StockHistoryEntity(
            String accountId,
            String sku,
            String eventId,
            String eventType,
            int previousQuantity,
            int newQuantity,
            int delta,
            String description,
            Instant occurredAt
    ) {
        this.accountId = accountId;
        this.sku = sku;
        this.eventId = eventId;
        this.eventType = eventType;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.delta = delta;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getPreviousQuantity() {
        return previousQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public int getDelta() {
        return delta;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
