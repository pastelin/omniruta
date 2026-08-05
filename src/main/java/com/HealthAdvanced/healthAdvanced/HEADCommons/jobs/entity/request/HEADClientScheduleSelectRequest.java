package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request;

import java.time.Instant;

public record HEADClientScheduleSelectRequest(
        Long jobId,
        Long scheduledTime
) {}
