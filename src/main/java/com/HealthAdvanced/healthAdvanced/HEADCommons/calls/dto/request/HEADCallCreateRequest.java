package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.request;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallContextType;

public record HEADCallCreateRequest(
        String toUuid,
        HEADChatParticipantType toType,
        HEADCallContextType contextType,
        String contextId
) {}
