package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;

public record HEADChatTypingUpdateDto(
        String conversationId,
        String fromUuid,
        boolean isTyping
) {}

