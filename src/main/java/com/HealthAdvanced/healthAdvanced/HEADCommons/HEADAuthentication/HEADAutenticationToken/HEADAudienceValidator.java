package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class HEADAudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String audience;

    public HEADAudienceValidator(String audience) { this.audience = audience; }

    @Override public OAuth2TokenValidatorResult validate(Jwt jwt) {
        return jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","invalid audience",""));
    }
}
