package com.HealthAdvanced.healthAdvanced.HEADPromotions.utils;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromotionAudience;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HEADPromotionAudienceUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static HEADPromotionAudience parse(String json) {
        if (json == null || json.isBlank()) return null;
        try { return MAPPER.readValue(json, HEADPromotionAudience.class); }
        catch (Exception e) { return null; }
    }
}
