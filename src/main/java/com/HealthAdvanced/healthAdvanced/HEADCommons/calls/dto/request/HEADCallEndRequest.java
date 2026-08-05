package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.request;

import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallEndReason;

public record HEADCallEndRequest(
        String callId,
        HEADCallEndReason reason
) {}