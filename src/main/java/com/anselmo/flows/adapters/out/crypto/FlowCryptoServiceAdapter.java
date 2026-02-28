package com.anselmo.flows.adapters.out.crypto;

import com.anselmo.flows.application.port.out.CryptoServicePort;
import com.anselmo.flows.domain.model.CryptoContext;
import com.anselmo.flows.domain.model.DecryptedFlowRequest;
import com.anselmo.flows.domain.model.EncryptedFlowRequest;
import com.anselmo.flows.domain.model.FlowResponse;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlowCryptoServiceAdapter implements CryptoServicePort {

    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final PrivateKey privateKey;
    private final String rsaTransformation;

    public FlowCryptoServiceAdapter(String privateKeyPem, String rsaTransformation) {
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
            DecryptedFlowRequest decryptedRequest = parseRequestJson(new String(decryptedData, StandardCharsets.UTF_8));

            return new DecryptedPayload(decryptedRequest, new CryptoContext(aesKey, iv));
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new CryptoOperationException("unable to decrypt request", ex);
        }
    }

    @Override
    public String encryptResponse(FlowResponse response, CryptoContext context) {
        try {
            byte[] plainResponse = toResponseJson(response).getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, context.aesKey(), new IvParameterSpec(context.iv()));
            byte[] encrypted = cipher.doFinal(plainResponse);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException ex) {
            throw new CryptoOperationException("unable to encrypt response", ex);
        }
    }

    private DecryptedFlowRequest parseRequestJson(String json) {
        String version = extractString(json, "version");
        String userLocale = extractString(json, "user_locale");
        String action = extractString(json, "action");
        String screen = extractString(json, "screen");
        String flowToken = extractString(json, "flow_token");
        return new DecryptedFlowRequest(version, userLocale, action, screen, Map.of(), flowToken);
    }

    private String toResponseJson(FlowResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"version\":\"").append(escape(response.version())).append("\",");
        sb.append("\"screen\":\"").append(escape(response.screen())).append("\",");
        sb.append("\"data\":{");

        boolean first = true;
        for (Map.Entry<String, Object> entry : response.data().entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append("\"").append(escape(entry.getKey())).append("\":\"")
                    .append(escape(String.valueOf(entry.getValue()))).append("\"");
        }
        sb.append("}}");
        return sb.toString();
    }

    private String extractString(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"(.*?)\\\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
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
