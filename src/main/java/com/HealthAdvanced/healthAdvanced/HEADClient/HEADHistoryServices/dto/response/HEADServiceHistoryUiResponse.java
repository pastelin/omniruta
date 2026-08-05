package com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.dto.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;

import java.util.List;

import java.util.List;

public record HEADServiceHistoryUiResponse(
        Summary summary,
        HEADPageResponse<Item> page
) {
    public record Summary(int total, int completed, double ratingAvg) {}

    public record Item(
            long id,
            String serviceType, // CONSULTATION, LABORATORY, PHYSIOTHERAPY, etc
            String serviceName,
            String date,        // "22 Feb 2026"
            String time,        // "10:00 AM"
            Professional professional,
            String status,      // COMPLETED, CANCELLED, NO_SHOW
            String location,
            String cost,        // "$850"
            Float rating,       // null si no hay
            String notes
    ) {}

    public record Professional(String name, String specialty, String initials) {}
}