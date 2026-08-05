package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

public record HEADJobAcceptedAfterCommitEvent(
        Long jobId,
        String staffUuid
) {}
