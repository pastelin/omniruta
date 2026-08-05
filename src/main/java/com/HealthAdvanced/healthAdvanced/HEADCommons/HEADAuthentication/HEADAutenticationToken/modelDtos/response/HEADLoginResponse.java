package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class HEADLoginResponse {
    private String accessToken;
    private Long   accessExpiresAt;  // epoch millis
    private String refreshToken;
    private String screenCurrent;
}