package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto;

import java.time.Instant;
import java.util.List;

public record HEADStaffScheduleProposeMultiRequest(
        Long jobId,
        String tz,
        int dayOffset,
        List<String> selectedStartAts,
        List<HEADStartAndEndTime> selectedStarEnd
) {}

