package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest;

public record HEADChatHistoryRequest(
        String conversationId,
        int page,
        int size
) {}