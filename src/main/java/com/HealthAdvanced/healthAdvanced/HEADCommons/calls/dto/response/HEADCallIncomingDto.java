package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallContextType;

public record HEADCallIncomingDto(
        String callId,
        String fromUuid,
        HEADChatParticipantType fromType,
        HEADCallContextType contextType,
        String contextId
) {}