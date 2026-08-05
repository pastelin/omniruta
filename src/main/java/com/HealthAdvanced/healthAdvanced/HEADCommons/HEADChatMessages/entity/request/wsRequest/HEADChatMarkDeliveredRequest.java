package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest;

public record HEADChatMarkDeliveredRequest(
        String conversationId,
        Long lastMessageId
) {}
