package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.response;

public record HEADRatingDistributionResponse(
        int stars,
        int count,
        int percentage
) {}