package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response;

import java.util.List;

public record HEADScheduleDayResponse(
        String date,
        HEADDayStatsResponse stats,
        List<HEADScheduledServiceResponse> services
) {}