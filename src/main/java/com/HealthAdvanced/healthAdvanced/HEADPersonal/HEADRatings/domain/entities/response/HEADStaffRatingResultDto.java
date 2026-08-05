package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.response;

public record HEADStaffRatingResultDto(
        Long staffUserId,
        String staffUuid,
        int totalReviews,
        int sumRating,
        double avgRating,
        double bayesianScore
) {}

