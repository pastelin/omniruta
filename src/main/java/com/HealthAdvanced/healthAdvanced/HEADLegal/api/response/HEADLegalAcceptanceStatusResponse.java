package com.HealthAdvanced.healthAdvanced.HEADLegal.api.response;

public record HEADLegalAcceptanceStatusResponse(
        boolean termsAccepted,
        boolean privacyAccepted
) {}