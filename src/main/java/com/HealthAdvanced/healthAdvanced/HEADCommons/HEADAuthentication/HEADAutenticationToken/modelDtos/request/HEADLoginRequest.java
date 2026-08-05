package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDtos.request;

import lombok.Data;

@Data
public class HEADLoginRequest {
    private Long userId;        // id numérico
    private String uuidUser;    // username (uuIdUser) → irá como subject del access token
}