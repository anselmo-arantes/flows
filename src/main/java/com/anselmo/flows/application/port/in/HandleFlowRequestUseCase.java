package com.anselmo.flows.application.port.in;

import com.anselmo.flows.domain.model.EncryptedFlowRequest;
import com.anselmo.flows.domain.model.EncryptedFlowResponse;

public interface HandleFlowRequestUseCase {

    EncryptedFlowResponse handle(EncryptedFlowRequest request);
}
