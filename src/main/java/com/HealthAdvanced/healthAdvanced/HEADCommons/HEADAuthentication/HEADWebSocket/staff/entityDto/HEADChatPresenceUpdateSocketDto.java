package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;

public record HEADChatPresenceUpdateSocketDto(
        String userUuid,
        HEADChatParticipantType userType,
        boolean online,
        boolean appActive,
        Long currentJobId,
        long lastUpdatedAt,
        String conversationId
) {}