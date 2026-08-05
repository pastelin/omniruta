package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;

public record HEADChatUnreadSummaryDto(
        String conversationId,
        long unreadCount
) {}

