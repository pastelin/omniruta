package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest;

public record HEADChatMarkReadRequest(
        String conversationId,
        Long lastMessageId
) {}