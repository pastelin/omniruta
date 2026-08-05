package com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.dto.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record HEADServiceHistoryGenericResponse(
        Summary summary,
        HEADPageResponse<Item> page
) {
    public record Summary(int total, int completed, double ratingAvg) {}

    public record Item(
            long id,
            String packageId,        // catálogo (estable)
            String serviceName,      // pkg.title
            String categoryLabel,    // pkg.subtitle
            String jobState,         // HEADJobState como String
            Instant when,            // instant (front formatea date/time)
            String location,         // startAddress o "Videollamada"
            BigDecimal amount,
            String currency,
            Professional professional,
            Float rating,            // null si no hay
            String notes,
            String iconUrl,
            List<String> gradientHex,
            Long occupationProfileId
    ) {}

    public record Professional(
            String name,
            String specialty,
            String initials
    ) {}
}