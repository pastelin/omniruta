package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response;

import java.util.List;

public record HEADTurnCredentialsResponse(
        String username,
        String credential,
        long expiresAt,
        List<HEADTurnIceServerResponse> iceServers
) {}