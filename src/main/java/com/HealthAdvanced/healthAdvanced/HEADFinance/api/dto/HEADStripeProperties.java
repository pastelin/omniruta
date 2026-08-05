package com.HealthAdvanced.healthAdvanced.HEADFinance.api.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
public record HEADStripeProperties(
        Webhook webhook,
        Connect connect
) {
    public record Webhook(
            String secret
    ) {}

    public record Connect(
            String refreshUrl,
            String returnUrl,
            String afterOnboardingRedirectUrl
    ) {}
}