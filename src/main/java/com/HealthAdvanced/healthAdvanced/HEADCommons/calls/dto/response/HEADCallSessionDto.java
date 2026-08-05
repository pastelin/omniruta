package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallContextType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallState;

public record HEADCallSessionDto(
        String callId,
        HEADCallState state,
        HEADCallContextType contextType,
        String contextId,
        String fromUuid,
        HEADChatParticipantType fromType,
        String toUuid,
        HEADChatParticipantType toType,
        long createdAt
) {}
