package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google")
public record HEADGoogleOauthProperties(
        oauth oauth
) {
    public record oauth(
            String clientId
    ) {}
}
