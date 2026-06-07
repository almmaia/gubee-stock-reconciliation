CREATE TABLE stocks (
    id              BIGSERIAL PRIMARY KEY,
    account_id      VARCHAR(100) NOT NULL,
    sku             VARCHAR(100) NOT NULL,
    available       INTEGER      NOT NULL DEFAULT 0,
    version         BIGINT       NOT NULL DEFAULT 0,
    last_updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_stocks_account_sku UNIQUE (account_id, sku),
    CONSTRAINT chk_stocks_available_non_negative CHECK (available >= 0)
);

CREATE TABLE processed_events (
    event_id        VARCHAR(100) PRIMARY KEY,
    event_type      VARCHAR(50)  NOT NULL,
    account_id      VARCHAR(100) NOT NULL,
    sku             VARCHAR(100),
    status          VARCHAR(30)  NOT NULL,
    result_message  TEXT,
    payload         JSONB        NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL,
    processed_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_processed_events_status ON processed_events (status);
CREATE INDEX idx_processed_events_account ON processed_events (account_id);

CREATE TABLE stock_history (
    id                BIGSERIAL PRIMARY KEY,
    account_id        VARCHAR(100) NOT NULL,
    sku               VARCHAR(100) NOT NULL,
    event_id          VARCHAR(100) NOT NULL,
    event_type        VARCHAR(50)  NOT NULL,
    previous_quantity INTEGER      NOT NULL,
    new_quantity      INTEGER      NOT NULL,
    delta             INTEGER      NOT NULL,
    description       TEXT         NOT NULL,
    occurred_at       TIMESTAMPTZ  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stock_history_account_sku ON stock_history (account_id, sku, occurred_at);

CREATE TABLE order_states (
    account_id        VARCHAR(100) NOT NULL,
    marketplace       VARCHAR(50)  NOT NULL,
    external_order_id VARCHAR(100) NOT NULL,
    sku               VARCHAR(100) NOT NULL,
    created_applied   BOOLEAN      NOT NULL DEFAULT FALSE,
    cancelled_applied BOOLEAN      NOT NULL DEFAULT FALSE,
    restored_applied  BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    PRIMARY KEY (account_id, marketplace, external_order_id, sku)
);

CREATE TABLE inconsistencies (
    id           BIGSERIAL PRIMARY KEY,
    event_id     VARCHAR(100) NOT NULL UNIQUE,
    account_id   VARCHAR(100) NOT NULL,
    sku          VARCHAR(100),
    reason       TEXT         NOT NULL,
    payload      JSONB        NOT NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
