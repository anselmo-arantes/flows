package com.anselmo.flows.domain.model;

public record EncryptedFlowRequest(
        String encryptedFlowData,
        String encryptedAesKey,
        String initialVector
) {
}
