import com.anselmo.flows.application.port.out.CryptoServicePort;
import com.anselmo.flows.application.service.FlowRequestHandlerService;
import com.anselmo.flows.domain.model.*;

import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

public class FlowRequestHandlerOfflineTest {
    public static void main(String[] args) {
        StubCryptoService stub = new StubCryptoService();
        FlowRequestHandlerService service = new FlowRequestHandlerService(stub);

        EncryptedFlowResponse result = service.handle(new EncryptedFlowRequest("data", "key", "iv"));

        if (!"encrypted-result".equals(result.encryptedFlowData())) {
            throw new IllegalStateException("Unexpected encrypted response");
        }
        if (!"resposta da API criada pelo Anselmo".equals(stub.capturedResponse.data().get("message"))) {
            throw new IllegalStateException("Unexpected message payload");
        }

        System.out.println("PASS: FlowRequestHandlerOfflineTest");
    }

    private static class StubCryptoService implements CryptoServicePort {
        private FlowResponse capturedResponse;

        @Override
        public DecryptedPayload decryptRequest(EncryptedFlowRequest request) {
            return new DecryptedPayload(
                    new DecryptedFlowRequest("1.0", "pt_BR", "init", "START", Map.of(), "token"),
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
