package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageType;

public record HEADChatSendMessageRequest(
        String conversationId,   // opcional, si viene vacío lo calculamos
        String recipientUuid,
        Long jobId,          // opcional
        String content,
        HEADChatMessageType type,
        Long fileAssetId
) { }
