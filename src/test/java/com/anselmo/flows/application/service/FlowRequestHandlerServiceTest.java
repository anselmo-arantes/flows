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
        StubCryptoService stub = new StubCryptoService();
        FlowRequestHandlerService service = new FlowRequestHandlerService(stub);

        EncryptedFlowResponse result = service.handle(new EncryptedFlowRequest("data", "key", "iv"));

        assertEquals("encrypted-result", result.encryptedFlowData());
        assertEquals("1.2", stub.capturedResponse.version());
        assertEquals("HOME", stub.capturedResponse.screen());
        assertEquals("resposta da API criada pelo Anselmo", stub.capturedResponse.data().get("message"));
    }


    @Test
    void shouldReturnPongForPingAction() {
        PingCryptoService stub = new PingCryptoService();
        FlowRequestHandlerService service = new FlowRequestHandlerService(stub);

        EncryptedFlowResponse result = service.handle(new EncryptedFlowRequest("data", "key", "iv"));

        assertEquals("encrypted-result", result.encryptedFlowData());
        assertEquals("pong", stub.capturedResponse.data().get("message"));
    }

    private static class StubCryptoService implements CryptoServicePort {
        private FlowResponse capturedResponse;

        @Override
        public DecryptedPayload decryptRequest(EncryptedFlowRequest request) {
            return new DecryptedPayload(
                    new DecryptedFlowRequest("1.2", "pt_BR", "init", "HOME", Map.of(), "token"),
                    new CryptoContext(new SecretKeySpec(new byte[16], "AES"), new byte[16])
            );
        }

        @Override
        public String encryptResponse(FlowResponse response, CryptoContext context) {
            this.capturedResponse = response;
            return "encrypted-result";
        }
    }

    private static class PingCryptoService implements CryptoServicePort {
        private FlowResponse capturedResponse;

        @Override
        public DecryptedPayload decryptRequest(EncryptedFlowRequest request) {
            return new DecryptedPayload(
                    new DecryptedFlowRequest("1.2", "pt_BR", "ping", "HOME", Map.of(), "token"),
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
