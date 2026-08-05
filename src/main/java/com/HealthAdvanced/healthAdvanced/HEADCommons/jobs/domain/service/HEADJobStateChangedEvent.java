package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;

import java.time.Instant;

public record HEADJobStateChangedEvent(Long jobId, HEADJobState prev, Instant at, String actorUuid) {
}
