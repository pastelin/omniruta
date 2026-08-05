package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response;

public record HEADMyScheduleResponse(
        HEADScheduleDayResponse today,
        HEADScheduleDayResponse tomorrow
) {}