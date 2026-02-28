package com.anselmo.flows.adapters.out.crypto;

import com.anselmo.flows.application.port.out.CryptoServicePort;
import com.anselmo.flows.domain.model.CryptoContext;
import com.anselmo.flows.domain.model.EncryptedFlowRequest;
import com.anselmo.flows.domain.model.FlowResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlowCryptoServiceAdapterTest {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    @Test
    void shouldDecryptAndEncryptRoundTrip() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        KeyPair keyPair = generateRsaKeyPair();
        String privatePem = toPkcs8Pem(keyPair);

        FlowCryptoServiceAdapter adapter = new FlowCryptoServiceAdapter(objectMapper, privatePem, RSA_TRANSFORMATION);

        SecretKey aesKey = generateAesKey();
        byte[] iv = "1234567890ABCDEF".getBytes(StandardCharsets.UTF_8);

        String requestJson = objectMapper.writeValueAsString(Map.of(
                "version", "3.0",
                "user_locale", "pt_BR",
                "action", "data_exchange",
                "screen", "START",
                "data", Map.of("field", "value"),
                "flow_token", "token"
        ));

        String encryptedFlowData = encryptAes(aesKey, iv, requestJson.getBytes(StandardCharsets.UTF_8));
        String encryptedAesKey = encryptRsa(keyPair, aesKey.getEncoded());

        EncryptedFlowRequest request = new EncryptedFlowRequest(encryptedFlowData, encryptedAesKey, Base64.getEncoder().encodeToString(iv));

        CryptoServicePort.DecryptedPayload payload = adapter.decryptRequest(request);

        assertEquals("3.0", payload.request().version());
        assertEquals("data_exchange", payload.request().action());

        FlowResponse flowResponse = new FlowResponse("3.0", "START", Map.of("message", "ok"));
        String encryptedResponse = adapter.encryptResponse(flowResponse, payload.context());

        String decryptedResponse = decryptAes(payload.context().aesKey(), payload.context().iv(), Base64.getDecoder().decode(encryptedResponse));
        assertEquals(objectMapper.writeValueAsString(flowResponse), decryptedResponse);
    }

    private static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static SecretKey generateAesKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private static String toPkcs8Pem(KeyPair keyPair) {
        String base64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----";
    }

    private static String encryptAes(SecretKey key, byte[] iv, byte[] payload) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        return Base64.getEncoder().encodeToString(cipher.doFinal(payload));
    }

    private static String decryptAes(SecretKey key, byte[] iv, byte[] payload) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return new String(cipher.doFinal(payload), StandardCharsets.UTF_8);
    }

    private static String encryptRsa(KeyPair keyPair, byte[] plain) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain));
    }
}
