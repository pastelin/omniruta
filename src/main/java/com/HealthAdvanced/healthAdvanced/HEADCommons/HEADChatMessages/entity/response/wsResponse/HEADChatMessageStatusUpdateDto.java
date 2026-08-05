package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageStatus;

import java.time.Instant;
import java.util.List;

public record HEADChatMessageStatusUpdateDto(
        String conversationId,
        List<Long> messageIds,
        HEADChatMessageStatus status,
        Instant updatedAt
) {}
