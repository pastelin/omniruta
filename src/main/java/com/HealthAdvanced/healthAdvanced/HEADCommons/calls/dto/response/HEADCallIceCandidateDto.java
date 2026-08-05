package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response;

public record HEADCallIceCandidateDto(
        String callId,
        String sdpMid,
        Integer sdpMLineIndex,
        String candidate
) {}
