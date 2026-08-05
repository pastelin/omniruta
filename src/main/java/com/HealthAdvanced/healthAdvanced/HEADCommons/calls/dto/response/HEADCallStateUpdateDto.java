package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallEndReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallState;

public record HEADCallStateUpdateDto(
        String callId,
        HEADCallState state,
        HEADCallEndReason endReason,
        long updatedAt
) {
}
