package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request;

public record HEADStaffScheduleProposeRequest(
        Long jobId,
        String tz,
        int dayOffset,
        String startTime,
        String endTime,
        int stepMin
) {}

