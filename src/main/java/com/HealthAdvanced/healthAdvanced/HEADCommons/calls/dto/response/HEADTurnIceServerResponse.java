package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response;

import java.util.List;

public record HEADTurnIceServerResponse(
        List<String> urls,
        String username,
        String credential
) {}
