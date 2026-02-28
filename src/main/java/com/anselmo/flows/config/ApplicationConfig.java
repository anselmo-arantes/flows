package com.anselmo.flows.config;

import com.anselmo.flows.adapters.out.crypto.FlowCryptoServiceAdapter;
import com.anselmo.flows.application.port.in.HandleFlowRequestUseCase;
import com.anselmo.flows.application.port.out.CryptoServicePort;
import com.anselmo.flows.application.service.FlowRequestHandlerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public CryptoServicePort cryptoServicePort(FlowCryptoServiceAdapter adapter) {
        return adapter;
    }

    @Bean
    public HandleFlowRequestUseCase handleFlowRequestUseCase(CryptoServicePort cryptoServicePort) {
        return new FlowRequestHandlerService(cryptoServicePort);
    }

    @Bean
    public FlowCryptoServiceAdapter flowCryptoServiceAdapter(org.springframework.core.env.Environment environment) {
        String privateKeyPem = environment.getProperty("flows.crypto.private-key-pem", "");
        String rsaTransformation = environment.getProperty("flows.crypto.rsa-transformation",
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        return new FlowCryptoServiceAdapter(privateKeyPem, rsaTransformation);
    }
}
