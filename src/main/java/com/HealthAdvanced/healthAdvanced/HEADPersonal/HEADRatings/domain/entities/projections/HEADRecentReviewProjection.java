package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.projections;

import java.time.Instant;

public interface HEADRecentReviewProjection {
    Long getId();
    String getNombre();
    String getPaterno();
    Integer getRating();
    String getComment();
    Instant getCreatedAt();
    String getServiceName();
}
