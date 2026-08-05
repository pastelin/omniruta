package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.response;

import java.util.Map;

public record HEADStaffRatingSummaryDto(
        long staffUserId,
        double avgRating,
        long totalReviews,
        double bayesianScore,
        Map<Integer, Long> starsBreakdown
) {}
