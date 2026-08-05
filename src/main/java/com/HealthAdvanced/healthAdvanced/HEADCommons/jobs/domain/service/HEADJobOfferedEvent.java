package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service;

import java.time.Instant;

public record HEADJobOfferedEvent(Long jobId,
                                  String staffUuid,
                                  Instant offeredAt) {
}
