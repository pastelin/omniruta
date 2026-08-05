package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest;

public record HEADChatMessageDeleteRequest(
        Long messageId,
        boolean deleteForAll
) {}

