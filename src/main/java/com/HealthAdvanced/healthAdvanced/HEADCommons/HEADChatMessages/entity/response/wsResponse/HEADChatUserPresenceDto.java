package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse;

import java.time.Instant;

public record HEADChatUserPresenceDto(
        String uuIdUser,
        boolean online,
        Instant lastSeenAt
) {}
