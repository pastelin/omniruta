package com.HealthAdvanced.healthAdvanced.HEADPromotions.utils;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromoTags;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class HEADCreativeUtils {

    private static final ObjectMapper OM = new ObjectMapper();

    private HEADCreativeUtils() {}

    public static HEADPromoTags parsePayload(String json) {
        if (json == null || json.isBlank()) return null;
        try { return OM.readValue(json, HEADPromoTags.class); }
        catch (Exception e) { return null; }
    }

    public static List<String> parseGradient(String json) {
        if (json == null || json.isBlank()) return null;
        try { return OM.readValue(json, new TypeReference<List<String>>() {}); }
        catch (Exception e) { return null; }
    }
}