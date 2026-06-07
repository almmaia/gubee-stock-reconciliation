package com.gubee.stock.domain.service;

import com.gubee.stock.domain.model.EventProcessingResult;
import com.gubee.stock.domain.model.EventProcessingStatus;
import com.gubee.stock.domain.model.EventType;
import com.gubee.stock.domain.model.OrderState;
import com.gubee.stock.domain.model.StockEvent;
import com.gubee.stock.domain.model.StockHistoryEntry;
import com.gubee.stock.domain.model.StockSnapshot;
import com.gubee.stock.domain.port.outbound.InconsistencyRepositoryPort;
import com.gubee.stock.domain.port.outbound.OrderStateRepositoryPort;
import com.gubee.stock.domain.port.outbound.ProcessedEventRepositoryPort;
import com.gubee.stock.domain.port.outbound.StockRepositoryPort;
import java.util.Objects;

public class StockEventProcessor {

    private final StockRepositoryPort stockRepository;
    private final ProcessedEventRepositoryPort processedEventRepository;
    private final OrderStateRepositoryPort orderStateRepository;
    private final InconsistencyRepositoryPort inconsistencyRepository;

    public StockEventProcessor(
            StockRepositoryPort stockRepository,
            ProcessedEventRepositoryPort processedEventRepository,
            OrderStateRepositoryPort orderStateRepository,
            InconsistencyRepositoryPort inconsistencyRepository
    ) {
        this.stockRepository = stockRepository;
        this.processedEventRepository = processedEventRepository;
        this.orderStateRepository = orderStateRepository;
        this.inconsistencyRepository = inconsistencyRepository;
    }

    public EventProcessingResult process(StockEvent event, String payload) {
        if (processedEventRepository.existsByEventId(event.eventId())) {
            StockSnapshot stock = stockRepository.findStock(event.accountId(), event.sku());
            return new EventProcessingResult(
                    event.eventId(),
                    EventProcessingStatus.IGNORED,
                    "Event already processed (idempotent by eventId)",
                    stock
            );
        }

        return switch (event.type()) {
            case STOCK_ADJUSTED -> processStockAdjusted(event, payload);
            case ORDER_CREATED -> processOrderCreated(event, payload);
            case ORDER_CANCELLED -> processOrderCancelled(event, payload);
            case MARKETPLACE_STOCK_RESTORED -> processMarketplaceRestored(event, payload);
            case STOCK_SYNC_SENT -> processStockSyncSent(event, payload);
        };
    }

    private EventProcessingResult processStockAdjusted(StockEvent event, String payload) {
        int available = Objects.requireNonNull(event.available(), "available is required for STOCK_ADJUSTED");
        StockSnapshot stock = stockRepository.getOrCreateStock(event.accountId(), event.sku());
        StockSnapshot updated = stockRepository.updateStock(event.accountId(), event.sku(), available, stock.version());
        appendHistory(
                event,
                stock.available(),
                updated.available(),
                "Manual stock adjustment to " + available + " (" + (event.reason() != null ? event.reason() : "no reason") + ")"
        );
        saveProcessed(event, EventProcessingStatus.PROCESSED, "Stock adjusted to " + available, payload);
        return result(event, EventProcessingStatus.PROCESSED, "Stock adjusted to " + available, updated);
    }

    private EventProcessingResult processOrderCreated(StockEvent event, String payload) {
        String marketplace = Objects.requireNonNull(event.marketplace(), "marketplace is required for ORDER_CREATED");
        String externalOrderId = Objects.requireNonNull(event.externalOrderId(), "externalOrderId is required for ORDER_CREATED");
        int quantity = requirePositive(event.quantity(), "quantity");

        OrderState orderState = orderStateRepository.find(event.accountId(), marketplace, externalOrderId, event.sku());
        if (orderState != null && orderState.createdApplied()) {
            saveProcessed(event, EventProcessingStatus.IGNORED, "Order creation already applied", payload);
            return result(event, EventProcessingStatus.IGNORED, "Order creation already applied (logical duplicate)");
        }

        if (orderState != null && orderState.cancelledApplied()) {
            orderStateRepository.save(orderState.withCreatedApplied());
            saveProcessed(
                    event,
                    EventProcessingStatus.PROCESSED,
                    "Order created after prior cancellation; no stock impact",
                    payload
            );
            StockSnapshot stock = stockRepository.findStock(event.accountId(), event.sku());
            int current = stock != null ? stock.available() : 0;
            appendHistory(
                    event,
                    current,
                    current,
                    "Order " + externalOrderId + " created after cancellation; stock unchanged"
            );
            return result(
                    event,
                    EventProcessingStatus.PROCESSED,
                    "Order created after prior cancellation; no stock impact",
                    stock
            );
        }

        StockSnapshot stock = stockRepository.getOrCreateStock(event.accountId(), event.sku());
        if (stock.available() < quantity) {
            inconsistencyRepository.save(event, "Insufficient stock for order creation", payload);
            saveProcessed(event, EventProcessingStatus.INCONSISTENT, "Insufficient stock", payload);
            return result(
                    event,
                    EventProcessingStatus.INCONSISTENT,
                    "Insufficient stock: available=" + stock.available() + ", requested=" + quantity,
                    stock
            );
        }

        StockSnapshot updated = stockRepository.updateStock(
                event.accountId(), event.sku(), stock.available() - quantity, stock.version()
        );
        OrderState state = orderState != null
                ? orderState.withCreatedApplied()
                : newOrderState(event, marketplace, externalOrderId).withCreatedApplied();
        orderStateRepository.save(state);
        appendHistory(event, stock.available(), updated.available(),
                "Order " + externalOrderId + " created; reserved " + quantity + " units");
        saveProcessed(event, EventProcessingStatus.PROCESSED, "Order created; stock decreased by " + quantity, payload);
        return result(event, EventProcessingStatus.PROCESSED, "Order created; stock decreased by " + quantity, updated);
    }

    private EventProcessingResult processOrderCancelled(StockEvent event, String payload) {
        String marketplace = Objects.requireNonNull(event.marketplace(), "marketplace is required for ORDER_CANCELLED");
        String externalOrderId = Objects.requireNonNull(event.externalOrderId(), "externalOrderId is required for ORDER_CANCELLED");
        int quantity = requirePositive(event.quantity(), "quantity");

        OrderState orderState = orderStateRepository.find(event.accountId(), marketplace, externalOrderId, event.sku());

        if (orderState != null && orderState.cancelledApplied()) {
            saveProcessed(event, EventProcessingStatus.IGNORED, "Cancellation already applied", payload);
            return result(event, EventProcessingStatus.IGNORED, "Duplicate cancellation ignored (logical duplicate)");
        }

        if (orderState == null || !orderState.createdApplied()) {
            OrderState updated = (orderState != null ? orderState : newOrderState(event, marketplace, externalOrderId))
                    .withCancelledApplied();
            orderStateRepository.save(updated);
            saveProcessed(
                    event,
                    EventProcessingStatus.PENDING,
                    "Cancellation received before order creation; held until order arrives",
                    payload
            );
            return result(
                    event,
                    EventProcessingStatus.PENDING,
                    "Cancellation received before order creation; no stock change yet"
            );
        }

        if (orderState.restoredApplied()) {
            orderStateRepository.save(orderState.withCancelledApplied());
            saveProcessed(
                    event,
                    EventProcessingStatus.PROCESSED,
                    "Cancellation acknowledged; marketplace already restored stock",
                    payload
            );
            StockSnapshot stock = stockRepository.findStock(event.accountId(), event.sku());
            int current = stock != null ? stock.available() : 0;
            appendHistory(
                    event,
                    current,
                    current,
                    "Order " + externalOrderId + " cancelled after marketplace restoration; no additional stock credit"
            );
            return result(
                    event,
                    EventProcessingStatus.PROCESSED,
                    "Cancellation acknowledged; marketplace already restored stock (no duplicate credit)",
                    stock
            );
        }

        StockSnapshot stock = stockRepository.getOrCreateStock(event.accountId(), event.sku());
        StockSnapshot updated = stockRepository.updateStock(
                event.accountId(), event.sku(), stock.available() + quantity, stock.version()
        );
        orderStateRepository.save(orderState.withCancelledApplied());
        appendHistory(event, stock.available(), updated.available(),
                "Order " + externalOrderId + " cancelled; returned " + quantity + " units");
        saveProcessed(event, EventProcessingStatus.PROCESSED, "Order cancelled; stock increased by " + quantity, payload);
        return result(event, EventProcessingStatus.PROCESSED, "Order cancelled; stock increased by " + quantity, updated);
    }

    private EventProcessingResult processMarketplaceRestored(StockEvent event, String payload) {
        String marketplace = Objects.requireNonNull(event.marketplace(), "marketplace is required for MARKETPLACE_STOCK_RESTORED");
        String externalOrderId = Objects.requireNonNull(event.externalOrderId(), "externalOrderId is required for MARKETPLACE_STOCK_RESTORED");
        int quantity = requirePositive(event.quantity(), "quantity");

        OrderState orderState = orderStateRepository.find(event.accountId(), marketplace, externalOrderId, event.sku());

        if (orderState != null && orderState.restoredApplied()) {
            saveProcessed(event, EventProcessingStatus.IGNORED, "Marketplace restoration already applied", payload);
            return result(event, EventProcessingStatus.IGNORED, "Duplicate marketplace restoration ignored");
        }

        if (orderState != null && orderState.cancelledApplied()) {
            orderStateRepository.save(orderState.withRestoredApplied());
            saveProcessed(event, EventProcessingStatus.IGNORED, "Restoration ignored; order already cancelled", payload);
            return result(event, EventProcessingStatus.IGNORED, "Restoration ignored; order already cancelled");
        }

        if (orderState == null || !orderState.createdApplied()) {
            saveProcessed(event, EventProcessingStatus.PENDING, "Restoration received before order creation", payload);
            return result(event, EventProcessingStatus.PENDING, "Restoration received before order creation");
        }

        StockSnapshot stock = stockRepository.getOrCreateStock(event.accountId(), event.sku());
        StockSnapshot updated = stockRepository.updateStock(
                event.accountId(), event.sku(), stock.available() + quantity, stock.version()
        );
        orderStateRepository.save(orderState.withRestoredApplied());
        appendHistory(
                event,
                stock.available(),
                updated.available(),
                "Marketplace restored " + quantity + " units for order " + externalOrderId
        );
        saveProcessed(event, EventProcessingStatus.PROCESSED, "Marketplace stock restored by " + quantity, payload);
        return result(event, EventProcessingStatus.PROCESSED, "Marketplace stock restored by " + quantity, updated);
    }

    private EventProcessingResult processStockSyncSent(StockEvent event, String payload) {
        int quantitySent = Objects.requireNonNull(event.quantitySent(), "quantitySent is required for STOCK_SYNC_SENT");
        StockSnapshot stock = stockRepository.findStock(event.accountId(), event.sku());
        int current = stock != null ? stock.available() : 0;
        appendHistory(
                event,
                current,
                current,
                "Stock sync sent to " + event.marketplace() + ": " + quantitySent + " units (audit only, no local change)"
        );
        saveProcessed(
                event,
                EventProcessingStatus.PROCESSED,
                "Sync event recorded; local stock unchanged (" + quantitySent + " sent)",
                payload
        );
        return result(
                event,
                EventProcessingStatus.PROCESSED,
                "Sync event recorded; local stock unchanged (" + quantitySent + " sent)",
                stock
        );
    }

    private void appendHistory(StockEvent event, int previous, int newQuantity, String description) {
        stockRepository.appendHistory(
                event.accountId(),
                event.sku(),
                new StockHistoryEntry(
                        event.eventId(),
                        event.type(),
                        previous,
                        newQuantity,
                        newQuantity - previous,
                        description,
                        event.occurredAt()
                )
        );
    }

    private void saveProcessed(StockEvent event, EventProcessingStatus status, String message, String payload) {
        processedEventRepository.save(event, status, message, payload);
    }

    private EventProcessingResult result(
            StockEvent event,
            EventProcessingStatus status,
            String message,
            StockSnapshot stock
    ) {
        return new EventProcessingResult(event.eventId(), status, message, stock);
    }

    private EventProcessingResult result(StockEvent event, EventProcessingStatus status, String message) {
        return new EventProcessingResult(event.eventId(), status, message);
    }

    private OrderState newOrderState(StockEvent event, String marketplace, String externalOrderId) {
        return new OrderState(event.accountId(), marketplace, externalOrderId, event.sku());
    }

    private int requirePositive(Integer value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
