package com.gubee.stock.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gubee.stock.domain.model.EventProcessingStatus;
import com.gubee.stock.domain.port.inbound.GetEventsByStatusUseCase;
import com.gubee.stock.domain.port.inbound.GetInconsistenciesUseCase;
import com.gubee.stock.domain.port.inbound.GetStockHistoryUseCase;
import com.gubee.stock.domain.port.inbound.GetStockUseCase;
import com.gubee.stock.domain.port.inbound.ProcessEventUseCase;
import com.gubee.stock.infrastructure.web.dto.StockEventRequest;
import com.gubee.stock.infrastructure.web.mapper.EventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping
@Tag(name = "Stock Reconciliation")
public class EventController {

    private final ProcessEventUseCase processEventUseCase;
    private final GetStockUseCase getStockUseCase;
    private final GetStockHistoryUseCase getStockHistoryUseCase;
    private final GetEventsByStatusUseCase getEventsByStatusUseCase;
    private final GetInconsistenciesUseCase getInconsistenciesUseCase;
    private final ObjectMapper objectMapper;

    public EventController(
            ProcessEventUseCase processEventUseCase,
            GetStockUseCase getStockUseCase,
            GetStockHistoryUseCase getStockHistoryUseCase,
            GetEventsByStatusUseCase getEventsByStatusUseCase,
            GetInconsistenciesUseCase getInconsistenciesUseCase,
            ObjectMapper objectMapper
    ) {
        this.processEventUseCase = processEventUseCase;
        this.getStockUseCase = getStockUseCase;
        this.getStockHistoryUseCase = getStockHistoryUseCase;
        this.getEventsByStatusUseCase = getEventsByStatusUseCase;
        this.getInconsistenciesUseCase = getInconsistenciesUseCase;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/events")
    @Operation(summary = "Receive and process a stock or order event")
    public ResponseEntity<?> processEvent(@Valid @RequestBody StockEventRequest request) {
        var event = EventMapper.toDomain(request);
        var payload = EventMapper.serializePayload(request, objectMapper);
        var result = processEventUseCase.process(event, payload);
        var response = EventMapper.toResponse(result);
        var status = result.status() == EventProcessingStatus.INCONSISTENT
                ? HttpStatus.CONFLICT
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/stocks/{accountId}/{sku}")
    @Operation(summary = "Get current stock for account and SKU")
    public ResponseEntity<?> getStock(@PathVariable String accountId, @PathVariable String sku) {
        var stock = getStockUseCase.getStock(accountId, sku);
        if (stock == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found for " + accountId + "/" + sku);
        }
        return ResponseEntity.ok(EventMapper.toStockResponse(stock));
    }

    @GetMapping("/stocks/{accountId}/{sku}/history")
    @Operation(summary = "Get stock change history")
    public ResponseEntity<?> getHistory(@PathVariable String accountId, @PathVariable String sku) {
        var history = getStockHistoryUseCase.getHistory(accountId, sku);
        return ResponseEntity.ok(history.stream().map(EventMapper::toHistoryResponse).toList());
    }

    @GetMapping("/events")
    @Operation(summary = "List processed events by status")
    public ResponseEntity<?> getEventsByStatus(@RequestParam EventProcessingStatus status) {
        var events = getEventsByStatusUseCase.getByStatus(status);
        return ResponseEntity.ok(events.stream().map(EventMapper::toProcessedEventResponse).toList());
    }

    @GetMapping("/inconsistencies")
    @Operation(summary = "List inconsistent events")
    public ResponseEntity<?> getInconsistencies() {
        var inconsistencies = getInconsistenciesUseCase.getAll();
        return ResponseEntity.ok(inconsistencies.stream().map(EventMapper::toInconsistencyResponse).toList());
    }
}
