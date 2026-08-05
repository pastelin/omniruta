package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.dto.response;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.enums.HEADCompletedServiceStatusResponse;

public record HEADCompletedServiceResponse(
        Long id,
        String patient,
        String type,
        String location,
        String date,
        String time,
        String duration,
        Integer amount,
        HEADCompletedServiceStatusResponse status
) {}