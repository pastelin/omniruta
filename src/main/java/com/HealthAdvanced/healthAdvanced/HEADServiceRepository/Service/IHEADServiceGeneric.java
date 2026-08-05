package com.HealthAdvanced.healthAdvanced.HEADServiceRepository.Service;

import org.springframework.web.reactive.function.client.WebClient;

public interface IHEADServiceGeneric {
    void changeBaseUrl(String url);
    WebClient webClientBuilder();
}
