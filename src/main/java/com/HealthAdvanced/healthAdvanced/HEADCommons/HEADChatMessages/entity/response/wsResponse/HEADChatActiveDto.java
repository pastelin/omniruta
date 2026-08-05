package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;

public record HEADChatActiveDto(
        String userUuid,                    // el que entró/salió
        HEADChatParticipantType userType,
        String conversationId,
        boolean inChat,                     // true=CHAT_ACTIVE, false=CHAT_INACTIVE
        long changedAt
) {}
