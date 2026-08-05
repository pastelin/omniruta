package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.dto;

import java.util.List;

public record HEADApiError(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        String folio,
        List<String> details
) {}