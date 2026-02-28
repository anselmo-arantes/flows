package com.anselmo.flows.adapters.in.web.dto;

import java.util.Map;

public record FlowErrorDto(
        String correlationId,
        String message,
        Map<String, String> fields
) {
}
