package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;

import java.util.List;

public record HEADMyRatingResponse(
        double overallRating,
        int totalReviews,
        List<HEADRatingDistributionResponse> ratingDistribution,
        HEADRatingStatsResponse stats,
        HEADPageResponse<HEADReviewViewResponse> reviews
) {}
