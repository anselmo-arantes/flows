package com.anselmo.flows.domain.model;

import java.util.Map;

public record DecryptedFlowRequest(
        String version,
        String userLocale,
        String action,
        String screen,
        Map<String, Object> data,
        String flowToken
) {
}
