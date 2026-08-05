package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.HEADChatMessageDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;

import java.util.List;

public record HEADChatOpenConversationDto(
        String conversationId,
        Long jobId,
        String callerUuid,
        HEADChatParticipantType callerType,
        String otherUuid,
        HEADChatParticipantType otherType,
        HEADChatUserProfileDto otherProfile,
        HEADChatPresenceDto otherPresence,
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<HEADChatMessageDto> items
) {}


