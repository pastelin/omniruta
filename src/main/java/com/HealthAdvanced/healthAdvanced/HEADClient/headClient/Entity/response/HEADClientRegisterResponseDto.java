package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADAppStateDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADClientRegisterResponseDto {
    private Boolean isRegisterUser;
    private String messageUser;
    private Boolean isSuccess;
    private String tokenSuccess;
    private Boolean isExistsClient;
    private HEADClientRegisterResponseInfoDto dataClient;
    private HEADStatusResponseDTO stepCurrent;
    private long expiresAt;
    private HEADAppStateDTO appStateDTO;
    private String refreshToken;
}
