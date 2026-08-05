package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADJwtUsersResponse;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADNextDTO;

public record HEADOtpVerifyRes(
        boolean ok, String code, String channel, Boolean isVerified, String identifier
) {
}