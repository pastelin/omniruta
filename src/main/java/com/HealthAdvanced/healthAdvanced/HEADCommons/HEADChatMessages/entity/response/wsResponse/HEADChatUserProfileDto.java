package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;

public record HEADChatUserProfileDto(
        String uuIdUser,
        HEADChatParticipantType type, // STAFF / CLIENT / SYSTEM
        String displayName,
        String avatarUrl
) {}
