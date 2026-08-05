package com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationsResponse;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromotionDto;

import java.util.List;

public record HEADDashboardInfoResponse(
        HEADNotificationsResponse.Summary summary,
        String etaTimeCurrent,
        HEADDashboardStatsDto stateTypeInfo,
        List<HEADPromotionDto> promotionsDash
) { }
