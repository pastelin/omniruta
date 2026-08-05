package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.HEADChatMessageDto;

import java.util.List;

public record HEADChatHistoryPageDto(
        String conversationId,
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<HEADChatMessageDto> items
) {}
