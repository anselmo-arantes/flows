package com.anselmo.flows.application.port.out;

import com.anselmo.flows.domain.model.CryptoContext;
import com.anselmo.flows.domain.model.DecryptedFlowRequest;
import com.anselmo.flows.domain.model.EncryptedFlowRequest;
import com.anselmo.flows.domain.model.FlowResponse;

public interface CryptoServicePort {

    DecryptedPayload decryptRequest(EncryptedFlowRequest request);

    String encryptResponse(FlowResponse response, CryptoContext context);

    record DecryptedPayload(DecryptedFlowRequest request, CryptoContext context) {
    }
}
