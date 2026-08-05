package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.response;

public record HEADReviewViewResponse(
        int id,
        String patient,
        int rating,
        String comment,
        String date,
        String service
) {}