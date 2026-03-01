package com.anselmo.flows.adapters.out.crypto;

import com.anselmo.flows.application.port.out.CryptoServicePort;
import com.anselmo.flows.domain.model.CryptoContext;
import com.anselmo.flows.domain.model.DecryptedFlowRequest;
import com.anselmo.flows.domain.model.EncryptedFlowRequest;
import com.anselmo.flows.domain.model.FlowResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import javax.crypto.Cipher;
import java.io.IOException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class FlowCryptoServiceAdapter implements CryptoServicePort {

    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final ObjectMapper objectMapper;
    private final PrivateKey privateKey;
    private final String rsaTransformation;

    public FlowCryptoServiceAdapter(ObjectMapper objectMapper, String privateKeyPem, String rsaTransformation) {
        this.objectMapper = objectMapper.copy().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.privateKey = toPrivateKey(privateKeyPem);
        this.rsaTransformation = rsaTransformation;
    }

    @Override
    public DecryptedPayload decryptRequest(EncryptedFlowRequest request) {
        try {
            byte[] encryptedAesKey = Base64.getDecoder().decode(request.encryptedAesKey());
            byte[] iv = Base64.getDecoder().decode(request.initialVector());
            byte[] encryptedData = Base64.getDecoder().decode(request.encryptedFlowData());

            SecretKey aesKey = decryptAesKey(encryptedAesKey);
            byte[] decryptedData = decryptAesPayload(aesKey, iv, encryptedData);
            DecryptedFlowRequest decryptedRequest = objectMapper.readValue(decryptedData, DecryptedFlowRequest.class);

            return new DecryptedPayload(decryptedRequest, new CryptoContext(aesKey, iv));
        } catch (IllegalArgumentException | GeneralSecurityException | IOException ex) {
            throw new CryptoOperationException("unable to decrypt request", ex);
        }
    }

    @Override
    public String encryptResponse(FlowResponse response, CryptoContext context) {
        try {
            byte[] plainResponse = objectMapper.writeValueAsBytes(response);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, context.aesKey(), new IvParameterSpec(context.iv()));
            byte[] encrypted = cipher.doFinal(plainResponse);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException | JsonProcessingException ex) {
            throw new CryptoOperationException("unable to encrypt response", ex);
        }
    }

    private SecretKey decryptAesKey(byte[] encryptedAesKey) throws GeneralSecurityException {
        Cipher rsaCipher = Cipher.getInstance(rsaTransformation);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        return new SecretKeySpec(aesKeyBytes, "AES");
    }

    private byte[] decryptAesPayload(SecretKey aesKey, byte[] iv, byte[] encryptedData) throws GeneralSecurityException {
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
        return aesCipher.doFinal(encryptedData);
    }

    private PrivateKey toPrivateKey(String pem) {
        try {
            String normalizedPem = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalizedPem.getBytes(StandardCharsets.UTF_8));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid RSA private key", ex);
        }
    }
}
