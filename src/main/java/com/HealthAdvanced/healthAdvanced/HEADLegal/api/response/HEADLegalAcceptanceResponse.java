package com.HealthAdvanced.healthAdvanced.HEADLegal.api.response;

import java.time.Instant;

public record HEADLegalAcceptanceResponse(
        Long legalDocumentId,
        String version,
        Instant acceptedAt,
        String platform,
        String deviceId
) {}