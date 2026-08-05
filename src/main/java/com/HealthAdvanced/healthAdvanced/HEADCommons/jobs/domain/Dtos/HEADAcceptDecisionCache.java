package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

import java.time.Instant;

public record HEADAcceptDecisionCache(
        Long jobId,
        Long staffId,
        String tz,
        Instant acceptedAt
) {}

