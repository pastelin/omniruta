package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos;

import java.util.Map;

public record HEADCurrentService(
        String serviceId,
        String status,
        String screenFlow,
        Map<String, Object> screenParams
) { }
