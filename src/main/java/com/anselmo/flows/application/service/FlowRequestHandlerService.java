package com.anselmo.flows.application.service;

import com.anselmo.flows.application.port.in.HandleFlowRequestUseCase;
import com.anselmo.flows.application.port.out.CryptoServicePort;
import com.anselmo.flows.domain.model.DecryptedFlowRequest;
import com.anselmo.flows.domain.model.EncryptedFlowRequest;
import com.anselmo.flows.domain.model.EncryptedFlowResponse;
import com.anselmo.flows.domain.model.FlowResponse;

import java.util.Map;

public class FlowRequestHandlerService implements HandleFlowRequestUseCase {

    private static final String DEFAULT_MESSAGE = "resposta da API criada pelo Anselmo";

    private final CryptoServicePort cryptoService;

    public FlowRequestHandlerService(CryptoServicePort cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Override
    public EncryptedFlowResponse handle(EncryptedFlowRequest request) {
        CryptoServicePort.DecryptedPayload decryptedPayload = cryptoService.decryptRequest(request);
        DecryptedFlowRequest decryptedRequest = decryptedPayload.request();

        FlowResponse response = new FlowResponse(
                decryptedRequest.version(),
                resolveScreen(decryptedRequest),
                Map.of("message", DEFAULT_MESSAGE)
        );

        String encrypted = cryptoService.encryptResponse(response, decryptedPayload.context());
        return new EncryptedFlowResponse(encrypted);
    }

    private String resolveScreen(DecryptedFlowRequest request) {
        if (request.screen() == null || request.screen().isBlank()) {
            return "START";
        }
        return request.screen();
    }
}
