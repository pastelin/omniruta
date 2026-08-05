package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest;

public record HEADChatTypingRequest(
        String conversationId,
        boolean isTyping
) {}
