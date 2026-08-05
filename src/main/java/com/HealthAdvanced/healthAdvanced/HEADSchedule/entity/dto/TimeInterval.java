package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.dto;

import java.time.Instant;

public record TimeInterval(Instant start, Instant end) {
    public boolean overlaps(Instant otherStart, Instant otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }
}

