package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response;

public record HEADDayStatsResponse(
        Integer total,
        Integer confirmed,
        Integer pending,
        Integer totalMinutes
) {}