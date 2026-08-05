package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response;

public record HEADProfileStatsResponse(
        String patients,
        String completedServices,
        String responseTime
) {}
