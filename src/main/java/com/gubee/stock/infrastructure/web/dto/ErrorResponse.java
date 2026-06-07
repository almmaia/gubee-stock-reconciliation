package com.gubee.stock.infrastructure.web.dto;

import java.util.List;

public record ErrorResponse(
        String message,
        List<String> details
) {
    public ErrorResponse(String message) {
        this(message, List.of());
    }
}
