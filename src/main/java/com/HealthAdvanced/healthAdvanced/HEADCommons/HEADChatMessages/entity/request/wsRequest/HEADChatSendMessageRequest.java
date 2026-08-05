package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageType;

public record HEADChatSendMessageRequest(
        String conversationId,
        String recipientUuid,
        Long jobId,
        String content,
        HEADChatMessageType type,
        Long fileAssetId
) {}
