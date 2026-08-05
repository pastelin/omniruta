package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.UiStatus;

import java.util.List;

public record HEADPrescriptionsResponse(
        Summary summary,
        List<Item> items
) {
    public record Summary(int total, int active, int completed, int expired) {}


    public record Item(
            Long id,
            String title,
            String folio,
            UiStatus status,
            String doctorName,
            String doctorSpecialty,
            Integer medicationsCount,
            Integer progressPercent, // null si no tracking
            String issuedDate,
            String validUntil
    ) {}
}