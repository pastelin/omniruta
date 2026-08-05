package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.request;

public record HEADSubmitReviewRequest(
        long jobId,
        int rating,      // 1..5
        String comment
) {}

