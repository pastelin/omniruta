package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;

import java.util.List;

public record HEADChatUnreadSummaryResponse(
        List<HEADChatUnreadSummaryDto> headChatUnreadSummaryDtoList,
        long total
) {
}
