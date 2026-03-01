package com.anselmo.flows.config;

import com.anselmo.flows.adapters.out.crypto.FlowCryptoServiceAdapter;
import com.anselmo.flows.application.port.in.HandleFlowRequestUseCase;
import com.anselmo.flows.application.service.FlowRequestHandlerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {


    @Bean
    public HandleFlowRequestUseCase handleFlowRequestUseCase(FlowCryptoServiceAdapter cryptoServiceAdapter) {
        return new FlowRequestHandlerService(cryptoServiceAdapter);
    }

    @Bean
    public FlowCryptoServiceAdapter flowCryptoServiceAdapter(ObjectMapper objectMapper,
                                                             org.springframework.core.env.Environment environment) {
        String privateKeyPem = environment.getProperty("flows.crypto.private-key-pem", "");
        String rsaTransformation = environment.getProperty("flows.crypto.rsa-transformation",
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        return new FlowCryptoServiceAdapter(objectMapper, privateKeyPem, rsaTransformation);
    }
}
