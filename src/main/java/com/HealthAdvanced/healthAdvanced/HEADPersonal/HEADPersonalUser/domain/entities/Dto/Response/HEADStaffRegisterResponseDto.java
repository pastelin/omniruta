package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADClientRegisterResponseInfoDto;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADAppStateDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADStaffRegisterResponseDto {
    private Boolean isRegisterUser;
    private String messageUser;
    private Boolean isSuccess;
    private String tokenSuccess;
    private Boolean isExistsStaff;
    private HEADStaffRegisterResponseInfoDto dataStaff;
    private HEADStatusResponseDTO stepCurrent;
    private long expiresAt;
    private HEADAppStateDTO appStateDTO;
    private String refreshToken;
}
