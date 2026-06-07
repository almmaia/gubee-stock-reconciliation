package com.gubee.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gubee.stock.domain.model.EventProcessingStatus;
import com.gubee.stock.domain.model.EventType;
import com.gubee.stock.domain.port.inbound.ProcessEventUseCase;
import com.gubee.stock.infrastructure.web.dto.StockEventRequest;
import com.gubee.stock.infrastructure.web.mapper.EventMapper;
import com.gubee.stock.support.PostgresTestContainerConfig;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StockReconciliationScenariosTest extends PostgresTestContainerConfig {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProcessEventUseCase processEventUseCase;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final String accountId = "account-001";
    private final String sku = "ABC-123";
    private final String marketplace = "MERCADO_LIVRE";
    private final Instant baseTime = Instant.parse("2026-05-28T10:00:00Z");

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute(
                "TRUNCATE TABLE stock_history, processed_events, order_states, inconsistencies, stocks RESTART IDENTITY CASCADE"
        );
    }

    private void postEvent(StockEventRequest request) throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private int currentStock() throws Exception {
        String response = mockMvc.perform(get("/stocks/{accountId}/{sku}", accountId, sku))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("available").asInt();
    }

    @Test
    void scenario1_initial_adjustment_sets_stock_to_ten() throws Exception {
        postEvent(new StockEventRequest(
                "evt-s1",
                EventType.STOCK_ADJUSTED,
                baseTime,
                accountId,
                sku,
                null,
                null,
                0,
                10,
                null,
                "manual_adjustment"
        ));
        Assertions.assertEquals(10, currentStock());
    }

    @Test
    void scenario2_order_decreases_stock_after_adjustment() throws Exception {
        postEvent(new StockEventRequest(
                "evt-s2-adj",
                EventType.STOCK_ADJUSTED,
                baseTime,
                accountId,
                sku,
                null,
                null,
                0,
                10,
                null,
                null
        ));
        postEvent(new StockEventRequest(
                "evt-s2-order",
                EventType.ORDER_CREATED,
                baseTime.plusSeconds(60),
                accountId,
                sku,
                marketplace,
                "ML-100",
                2,
                null,
                null,
                null
        ));
        Assertions.assertEquals(8, currentStock());
    }

    @Test
    void scenario3_cancellation_restores_stock() throws Exception {
        postEvent(new StockEventRequest(
                "evt-s3-adj",
                EventType.STOCK_ADJUSTED,
                baseTime,
                accountId,
                sku,
                null,
                null,
                0,
                10,
                null,
                null
        ));
        postEvent(new StockEventRequest(
                "evt-s3-order",
                EventType.ORDER_CREATED,
                baseTime.plusSeconds(60),
                accountId,
                sku,
                marketplace,
                "ML-200",
                2,
                null,
                null,
                null
        ));
        postEvent(new StockEventRequest(
                "evt-s3-cancel",
                EventType.ORDER_CANCELLED,
                baseTime.plusSeconds(120),
                accountId,
                sku,
                marketplace,
                "ML-200",
                2,
                null,
                null,
                null
        ));
        Assertions.assertEquals(10, currentStock());
    }

    @Test
    void scenario4_duplicate_eventId_is_ignored() throws Exception {
        StockEventRequest event = new StockEventRequest(
                "evt-s4-dup",
                EventType.STOCK_ADJUSTED,
                baseTime,
                accountId,
                sku,
                null,
                null,
                0,
                10,
                null,
                null
        );
        postEvent(event);
        postEvent(new StockEventRequest(
                "evt-s4-dup",
                EventType.STOCK_ADJUSTED,
                baseTime,
                accountId,
                sku,
                null,
                null,
                0,
                99,
                null,
                null
        ));
        Assertions.assertEquals(10, currentStock());
    }

    @Test
    void scenario5_duplicate_cancellation_does_not_restore_twice() throws Exception {
        postEvent(new StockEventRequest(
                "evt-s5-adj",
                EventType.STOCK_ADJUSTED,
                baseTime,
                accountId,
                sku,
                null,
                null,
                0,
                10,
                null,
                null
        ));
        postEvent(new StockEventRequest(
                "evt-s5-order",
                EventType.ORDER_CREATED,
                baseTime.plusSeconds(60),
                accountId,
                sku,
                marketplace,
                "ML-300",
                2,
                null,
                null,
                null
        ));
        postEvent(new StockEventRequest(
                "evt-s5-cancel-1",
                EventType.ORDER_CANCELLED,
                baseTime.plusSeconds(120),
                accountId,
                sku,
                marketplace,
                "ML-300",
                2,
                null,
                null,
                null
        ));
        postEvent(new StockEventRequest(
                "evt-s5-cancel-2",
                EventType.ORDER_CANCELLED,
                baseTime.plusSeconds(180),
                accountId,
                sku,
                marketplace,
                "ML-300",
                2,
                null,
                null,
                null
        ));
        Assertions.assertEquals(10, currentStock());
    }

    @Test
    void scenario6_cancellation_before_order_is_pending() throws Exception {
        var cancelResult = processEventUseCase.process(
                EventMapper.toDomain(new StockEventRequest(
                        "evt-s6-cancel",
                        EventType.ORDER_CANCELLED,
                        baseTime,
                        accountId,
                        sku,
                        marketplace,
                        "ML-400",
                        2,
                        null,
                        null,
                        null
                )),
                "{}"
        );
        Assertions.assertEquals(EventProcessingStatus.PENDING, cancelResult.status());

        postEvent(new StockEventRequest(
                "evt-s6-adj",
                EventType.STOCK_ADJUSTED,
                baseTime.plusSeconds(30),
                accountId,
                sku,
                null,
                null,
                0,
                10,
                null,
                null
        ));

        var orderResult = processEventUseCase.process(
                EventMapper.toDomain(new StockEventRequest(
                        "evt-s6-order",
                        EventType.ORDER_CREATED,
                        baseTime.plusSeconds(60),
                        accountId,
                        sku,
                        marketplace,
                        "ML-400",
                        2,
                        null,
                        null,
                        null
                )),
                "{}"
        );
        Assertions.assertEquals(EventProcessingStatus.PROCESSED, orderResult.status());
        Assertions.assertEquals(10, currentStock());
    }

    @Test
    void scenario7_concurrent_orders_do_not_create_negative_stock() throws Exception {
        postEvent(new StockEventRequest(
                "evt-s7-adj",
                EventType.STOCK_ADJUSTED,
                baseTime,
                accountId,
                sku,
                null,
                null,
                0,
                5,
                null,
                null
        ));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Callable<Object>> tasks = List.of(
                () -> processEventUseCase.process(
                        EventMapper.toDomain(new StockEventRequest(
                                "evt-s7-order-a",
                                EventType.ORDER_CREATED,
                                baseTime.plusSeconds(10),
                                accountId,
                                sku,
                                marketplace,
                                "ML-501",
                                3,
                                null,
                                null,
                                null
                        )),
                        "{}"
                ),
                () -> processEventUseCase.process(
                        EventMapper.toDomain(new StockEventRequest(
                                "evt-s7-order-b",
                                EventType.ORDER_CREATED,
                                baseTime.plusSeconds(10),
                                accountId,
                                sku,
                                marketplace,
                                "ML-502",
                                3,
                                null,
                                null,
                                null
                        )),
                        "{}"
                )
        );

        List<java.util.concurrent.Future<Object>> results = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
        executor.shutdown();

        long processedCount = results.stream()
                .map(future -> {
                    try {
                        return ((com.gubee.stock.domain.model.EventProcessingResult) future.get()).status();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .filter(status -> status == EventProcessingStatus.PROCESSED)
                .count();

        long inconsistentCount = results.stream()
                .map(future -> {
                    try {
                        return ((com.gubee.stock.domain.model.EventProcessingResult) future.get()).status();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .filter(status -> status == EventProcessingStatus.INCONSISTENT)
                .count();

        Assertions.assertEquals(1, processedCount);
        Assertions.assertEquals(1, inconsistentCount);
        Assertions.assertTrue(currentStock() >= 0);
        Assertions.assertEquals(2, currentStock());
    }

    @Test
    void scenario8_marketplace_restore_then_cancel_does_not_duplicate_restore() throws Exception {
        postEvent(new StockEventRequest(
                "evt-s8-adj",
                EventType.STOCK_ADJUSTED,
                baseTime,
                accountId,
                sku,
                null,
                null,
                0,
                10,
                null,
                null
        ));
        postEvent(new StockEventRequest(
                "evt-s8-order",
                EventType.ORDER_CREATED,
                baseTime.plusSeconds(60),
                accountId,
                sku,
                marketplace,
                "ML-600",
                2,
                null,
                null,
                null
        ));
        Assertions.assertEquals(8, currentStock());

        postEvent(new StockEventRequest(
                "evt-s8-restore",
                EventType.MARKETPLACE_STOCK_RESTORED,
                baseTime.plusSeconds(120),
                accountId,
                sku,
                marketplace,
                "ML-600",
                2,
                null,
                null,
                null
        ));
        Assertions.assertEquals(10, currentStock());

        var cancelResult = processEventUseCase.process(
                EventMapper.toDomain(new StockEventRequest(
                        "evt-s8-cancel",
                        EventType.ORDER_CANCELLED,
                        baseTime.plusSeconds(180),
                        accountId,
                        sku,
                        marketplace,
                        "ML-600",
                        2,
                        null,
                        null,
                        null
                )),
                "{}"
        );
        Assertions.assertEquals(EventProcessingStatus.PROCESSED, cancelResult.status());
        Assertions.assertEquals(10, currentStock());
    }
}
