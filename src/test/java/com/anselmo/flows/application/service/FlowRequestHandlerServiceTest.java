package com.anselmo.flows.application.service;

import com.anselmo.flows.application.port.out.CryptoServicePort;
import com.anselmo.flows.domain.model.CryptoContext;
import com.anselmo.flows.domain.model.DecryptedFlowRequest;
import com.anselmo.flows.domain.model.EncryptedFlowRequest;
import com.anselmo.flows.domain.model.EncryptedFlowResponse;
import com.anselmo.flows.domain.model.FlowResponse;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlowRequestHandlerServiceTest {

    @Test
    void shouldBuildExpectedFlowResponseAndEncrypt() {
        StubCryptoService stub = new StubCryptoService(new DecryptedFlowRequest("1.2", "pt_BR", "init", "HOME", Map.of(), "token"));
        FlowRequestHandlerService service = new FlowRequestHandlerService(stub);

        EncryptedFlowResponse result = service.handle(new EncryptedFlowRequest("data", "key", "iv"));

        assertEquals("encrypted-result", result.encryptedFlowData());
        assertEquals("1.2", stub.capturedResponse.version());
        assertEquals("HOME", stub.capturedResponse.screen());
        assertEquals("resposta da API criada pelo Anselmo", stub.capturedResponse.data().get("message"));
    }

    @Test
    void shouldUseStartWhenScreenIsBlank() {
        StubCryptoService stub = new StubCryptoService(new DecryptedFlowRequest("1.2", "pt_BR", "init", "  ", Map.of(), "token"));
        FlowRequestHandlerService service = new FlowRequestHandlerService(stub);

        service.handle(new EncryptedFlowRequest("data", "key", "iv"));

        assertEquals("START", stub.capturedResponse.screen());
    }

    @Test
    void shouldUseFallbackVersionWhenBlank() {
        StubCryptoService stub = new StubCryptoService(new DecryptedFlowRequest("", "pt_BR", "init", "HOME", Map.of(), "token"));
        FlowRequestHandlerService service = new FlowRequestHandlerService(stub);

        service.handle(new EncryptedFlowRequest("data", "key", "iv"));

        assertEquals("1.0", stub.capturedResponse.version());
    }

    @Test
    void shouldReturnPongForPingAction() {
        StubCryptoService stub = new StubCryptoService(new DecryptedFlowRequest("1.2", "pt_BR", "ping", "HOME", Map.of(), "token"));
        FlowRequestHandlerService service = new FlowRequestHandlerService(stub);

        EncryptedFlowResponse result = service.handle(new EncryptedFlowRequest("data", "key", "iv"));

        assertEquals("encrypted-result", result.encryptedFlowData());
        assertEquals("pong", stub.capturedResponse.data().get("message"));
    }

    private static class StubCryptoService implements CryptoServicePort {
        private final DecryptedFlowRequest decryptedFlowRequest;
        private FlowResponse capturedResponse;

        private StubCryptoService(DecryptedFlowRequest decryptedFlowRequest) {
            this.decryptedFlowRequest = decryptedFlowRequest;
        }

        @Override
        public DecryptedPayload decryptRequest(EncryptedFlowRequest request) {
            return new DecryptedPayload(
                    decryptedFlowRequest,
                    new CryptoContext(new SecretKeySpec(new byte[16], "AES"), new byte[16])
            );
        }

        @Override
        public String encryptResponse(FlowResponse response, CryptoContext context) {
            this.capturedResponse = response;
            return "encrypted-result";
        }
    }
}
