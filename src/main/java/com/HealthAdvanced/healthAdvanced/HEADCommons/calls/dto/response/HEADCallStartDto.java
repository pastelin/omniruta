package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallContextType;

public record HEADCallStartDto(
        String callId,
        Long jobId,
        boolean shouldCreateOffer, // <- decidido por el server para este receptor
        HEADCallContextType contextType,
        String contextId
) {}

