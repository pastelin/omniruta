package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class HEADJobSnapshotDto {
    Long jobId;
    String state;

    Instant assignedAt;
    Instant acceptedAt;
    Instant arrivedAt;
    Instant startedAt;
    Instant completedAt;
    Instant cancelledAt;

    String  cancelledBy;      // CLIENT / STAFF / SYSTEM
    String  cancelReason;

    BigDecimal amount;
    String  currency;
    Integer distanceMeters;
    Integer durationSeconds;

    Long clientId;            // opcional (para front)
    String staffUuid;         // opcional (para front)
}

