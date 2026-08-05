package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADSdpType;

public record HEADCallSdpDto(
    String callId,
    HEADSdpType type,
    String sdp
) {}
