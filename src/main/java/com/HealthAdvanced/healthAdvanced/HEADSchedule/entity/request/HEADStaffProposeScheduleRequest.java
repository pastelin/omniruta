package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.request;

import java.time.LocalDate;

public record HEADStaffProposeScheduleRequest(
        Long jobId,
        String tz,                 // "America/Mexico_City"
        int dayOffset,            // hoy/mañana
        String startTime,          // "08:00"
        String endTime,            // "18:00"
        int stepMin               // 30 o 60
) {}

