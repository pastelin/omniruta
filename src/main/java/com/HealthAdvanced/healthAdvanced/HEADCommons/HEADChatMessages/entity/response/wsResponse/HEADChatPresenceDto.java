package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;

public record HEADChatPresenceDto(
        String userUuid,
        HEADChatParticipantType userType,
        boolean online,       // conectado al socket (presence:...)
        boolean appActive,    // app abierta / en foreground (para client) o online staff
        Long currentJobId,
        long lastUpdatedAt,
        String activeConversationId
) {}