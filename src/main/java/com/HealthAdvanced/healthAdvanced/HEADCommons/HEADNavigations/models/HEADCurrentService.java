package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.models;

import java.util.Map;

public record HEADCurrentService(
        String serviceId,
        String status,
        String screenFlow,
        Map<String, Object> screenParams
) {}

