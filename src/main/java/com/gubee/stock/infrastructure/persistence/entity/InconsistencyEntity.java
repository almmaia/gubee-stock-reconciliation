package com.gubee.stock.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "inconsistencies")
public class InconsistencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column
    private String sku;

    @Column(nullable = false)
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected InconsistencyEntity() {
    }

    public InconsistencyEntity(
            String eventId,
            String accountId,
            String sku,
            String reason,
            String payload,
            Instant occurredAt
    ) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.sku = sku;
        this.reason = reason;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getSku() {
        return sku;
    }

    public String getReason() {
        return reason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
