package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response;

import java.util.List;

public record HEADScheduleSlotsResponse(
        String tz,
        int durationMin,
        List<HEADDaySlots> days
) {}
