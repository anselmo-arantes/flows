package com.anselmo.flows.adapters.in.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FlowControllerIntegrationTest {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private static final KeyPair keyPair = createKeyPair();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("flows.crypto.private-key-pem", () -> {
            String encoded = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            return "-----BEGIN PRIVATE KEY-----\n" + encoded + "\n-----END PRIVATE KEY-----";
        });
    }

    @Test
    void shouldProcessEncryptedRequestAndReturnEncryptedResponse() throws Exception {
        SecretKey aesKey = generateAesKey();
        byte[] iv = "1234567890ABCDEF".getBytes(StandardCharsets.UTF_8);

        String plainPayload = objectMapper.writeValueAsString(Map.of(
                "version", "7.1",
                "user_locale", "pt_BR",
                "action", "init",
                "screen", "START",
                "data", Map.of("name", "anselmo"),
                "flow_token", "token"
        ));

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "encrypted_flow_data", encryptAes(aesKey, iv, plainPayload.getBytes(StandardCharsets.UTF_8)),
                "encrypted_aes_key", encryptRsa(aesKey.getEncoded()),
                "initial_vector", Base64.getEncoder().encodeToString(iv)
        ));

        String responseBody = mockMvc.perform(post("/v1/flows/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String encryptedResponse = jsonNode.get("encrypted_flow_data").asText();
        String decryptedResponse = decryptAes(aesKey, iv, Base64.getDecoder().decode(encryptedResponse));

        JsonNode responseJson = objectMapper.readTree(decryptedResponse);
        assertThat(responseJson.get("version").asText()).isEqualTo("7.1");
        assertThat(responseJson.get("screen").asText()).isEqualTo("START");
        assertThat(responseJson.get("data").get("message").asText()).isEqualTo("resposta da API criada pelo Anselmo");
    }


    private static KeyPair createKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("unable to create RSA key pair for tests", ex);
        }
    }
    private SecretKey generateAesKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private String encryptRsa(byte[] plain) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain));
    }

    private String encryptAes(SecretKey key, byte[] iv, byte[] payload) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        return Base64.getEncoder().encodeToString(cipher.doFinal(payload));
    }

    private String decryptAes(SecretKey key, byte[] iv, byte[] payload) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return new String(cipher.doFinal(payload), StandardCharsets.UTF_8);
    }
}
