package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.dto;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStartAndEndTime;

import java.util.List;

public record HEADScheduleProposalCache(
        Long jobId,
        String staffUuid,
        String tz,
        int dayOffset,
        List<Long> selectedStartAtMs,
        long createdAtMs,
        long expiresAtMs,
        List<HEADStartAndEndTime> selectedStarEnd,
        int durationMin
) {}

