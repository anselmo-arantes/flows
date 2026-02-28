package com.anselmo.flows.application.service;

import com.anselmo.flows.domain.model.FlowResponse;

import java.util.Map;

public class FlowErrorResponseFactory {

    private FlowErrorResponseFactory() {
    }

    public static FlowResponse genericError() {
        return new FlowResponse("1.0", "START", Map.of("error", "unable_to_process_request"));
    }
}
