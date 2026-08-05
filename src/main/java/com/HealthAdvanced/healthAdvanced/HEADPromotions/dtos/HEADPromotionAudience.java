package com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HEADPromotionAudience {
    public String audience;            // "NEW_USER"
    public Integer minCompletedJobs;    // 0
    public Integer maxCompletedJobs;    // 0
    public Integer inactiveDays;        // futuro
}