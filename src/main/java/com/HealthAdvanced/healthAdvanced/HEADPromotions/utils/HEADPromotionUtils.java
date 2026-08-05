package com.HealthAdvanced.healthAdvanced.HEADPromotions.utils;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromoTags;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HEADPromotionUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static HEADPromoTags parseTagsJson(String tags) {
        if (tags == null || tags.isBlank()) return null;
        try { return MAPPER.readValue(tags, HEADPromoTags.class); }
        catch (Exception e) { return null; }
    }
}
