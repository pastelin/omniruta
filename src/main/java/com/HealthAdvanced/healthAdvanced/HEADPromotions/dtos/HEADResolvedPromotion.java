package com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardVariant;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionTargetType;

public record HEADResolvedPromotion(
        Long id,
        HEADPromotionTargetType targetType,
        String targetId,
        String label,
        Integer percent,
        HEADCardVariant uiVariant,
        Integer priority
) {}