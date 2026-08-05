package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest;

import java.util.List;

public record HEADChatPresenceSubscribeRequest(
        List<String> uuids
) {}
