package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response;


import com.HealthAdvanced.healthAdvanced.HEADSchedule.enums.HEADServiceStatusResponse;

public record HEADScheduledServiceResponse(
        Long id,
        String time,
        String date,
        String patient,
        String type,
        String location,
        String address,
        HEADServiceStatusResponse status,
        Integer durationMinutes,
        Boolean isVideoCall,
        String description,
        Double lat,
        Double lng
) {}