package com.anselmo.flows.adapters.in.http;

import com.anselmo.flows.adapters.in.http.dto.EncryptedFlowRequestDto;
import com.anselmo.flows.adapters.in.http.dto.EncryptedFlowResponseDto;
import com.anselmo.flows.application.port.in.HandleFlowRequestUseCase;
import com.anselmo.flows.domain.model.EncryptedFlowRequest;
import com.anselmo.flows.domain.model.EncryptedFlowResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/flows")
public class FlowController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowController.class);

    private final HandleFlowRequestUseCase handleFlowRequestUseCase;
    private final MeterRegistry meterRegistry;

    public FlowController(HandleFlowRequestUseCase handleFlowRequestUseCase, MeterRegistry meterRegistry) {
        this.handleFlowRequestUseCase = handleFlowRequestUseCase;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping(path = "/data", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public EncryptedFlowResponseDto handleDataExchange(@Valid @RequestBody EncryptedFlowRequestDto requestDto) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            EncryptedFlowRequest request = new EncryptedFlowRequest(
                    requestDto.encrypted_flow_data(),
                    requestDto.encrypted_aes_key(),
                    requestDto.initial_vector()
            );
            EncryptedFlowResponse response = handleFlowRequestUseCase.handle(request);
            return new EncryptedFlowResponseDto(response.encryptedFlowData());
        } finally {
            sample.stop(meterRegistry.timer("flows.data_exchange.latency"));
            LOGGER.info("processed flows data exchange request correlationId={}", MDC.get("correlationId"));
        }
    }
}
