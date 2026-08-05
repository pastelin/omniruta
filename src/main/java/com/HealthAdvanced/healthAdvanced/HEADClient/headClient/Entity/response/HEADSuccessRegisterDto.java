package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADAppStateDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
public class HEADSuccessRegisterDto {
    private HEADStatusResponseDTO stepCurrent;
    private String accessToken;
    private long expiresAt;
    private String refreshToken;
}
