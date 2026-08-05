package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.request;

public record HEADCreateStaffReviewRequest(
        Long jobId,
        Integer rating,
        String comment
) {}

