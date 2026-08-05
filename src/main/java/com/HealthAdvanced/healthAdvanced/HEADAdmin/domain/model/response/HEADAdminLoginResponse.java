package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HEADAdminLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private HEADAdminResponse admin;
}