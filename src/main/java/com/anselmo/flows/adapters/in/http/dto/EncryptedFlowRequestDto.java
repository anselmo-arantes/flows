package com.anselmo.flows.adapters.in.http.dto;

import jakarta.validation.constraints.NotBlank;

public record EncryptedFlowRequestDto(
        @NotBlank(message = "encrypted_flow_data is required")
        String encrypted_flow_data,
        @NotBlank(message = "encrypted_aes_key is required")
        String encrypted_aes_key,
        @NotBlank(message = "initial_vector is required")
        String initial_vector
) {
}
