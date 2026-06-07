package com.gubee.stock.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class PostgresTestContainerConfig {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5435/stock_reconciliation");
        registry.add("spring.datasource.username", () -> "stock");
        registry.add("spring.datasource.password", () -> "stock");
    }
}
