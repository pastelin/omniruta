package com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotion;

import java.util.Map;

public record HEADResolvedPromos(
        HEADPromotion bestProfilePromo,
        Map<String, HEADPromotion> bestByPackage
) {
    public static HEADResolvedPromos empty() {
        return new HEADResolvedPromos(null, java.util.Map.of());
    }
}