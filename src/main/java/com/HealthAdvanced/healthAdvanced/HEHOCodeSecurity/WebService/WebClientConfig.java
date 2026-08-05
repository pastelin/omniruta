package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.WebService;

import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.propertiesModel.TelnyxProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient telnyxWebClient(TelnyxProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}