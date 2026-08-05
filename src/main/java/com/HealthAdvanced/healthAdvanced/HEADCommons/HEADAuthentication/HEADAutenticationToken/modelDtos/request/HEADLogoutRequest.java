package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDtos.request;

import lombok.Data;

@Data
public class HEADLogoutRequest {
    private Long userId;
    private String deviceId;
}