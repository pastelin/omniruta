package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response;

import java.time.Instant;

public record HEADSlot(
        String time,               // "09:00"
        Instant startAt,
        Instant endAt,
        boolean available,
        String reason              // "BOOKED" | "BUFFER" | "OUT_OF_RANGE" | null
) {}
