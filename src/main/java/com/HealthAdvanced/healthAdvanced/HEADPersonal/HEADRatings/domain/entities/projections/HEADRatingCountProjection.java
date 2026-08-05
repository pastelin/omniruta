package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.projections;

public interface HEADRatingCountProjection {
    Integer getRating(); // 1..5
    Long getCnt();
}
