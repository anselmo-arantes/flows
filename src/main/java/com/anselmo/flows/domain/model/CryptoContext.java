package com.anselmo.flows.domain.model;

import javax.crypto.SecretKey;

public record CryptoContext(SecretKey aesKey, byte[] iv) {
}
