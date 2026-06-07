package com.gubee.stock.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "processed_events")
public class ProcessedEventEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column
    private String sku;

    @Column(nullable = false)
    private String status;

    @Column(name = "result_message")
    private String resultMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt = Instant.now();

    protected ProcessedEventEntity() {
    }

    public ProcessedEventEntity(
            String eventId,
            String eventType,
            String accountId,
            String sku,
            String status,
            String resultMessage,
            String payload,
            Instant occurredAt
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.accountId = accountId;
        this.sku = sku;
        this.status = status;
        this.resultMessage = resultMessage;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getSku() {
        return sku;
    }

    public String getStatus() {
        return status;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
