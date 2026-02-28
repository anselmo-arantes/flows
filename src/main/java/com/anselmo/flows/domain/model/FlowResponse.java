package com.anselmo.flows.domain.model;

import java.util.Map;

public record FlowResponse(
        String version,
        String screen,
        Map<String, Object> data
) {
}
