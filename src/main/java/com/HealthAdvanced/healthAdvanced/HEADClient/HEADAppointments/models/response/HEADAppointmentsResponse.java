package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAppointments.models.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;

import java.time.Instant;
import java.util.List;

public record HEADAppointmentsResponse(
        boolean success,
        Data data
) {
    public record Data(
            List<AppointmentItem> items,
            int page,
            int size,
            long total
    ) {}

    public record AppointmentItem(
            String id,
            String title,
            AppointmentStatus status,
            String providerName,
            String providerCategory,
            Instant scheduledAt,
            HEADServiceMode mode,
            String locationName
    ) {}

    public enum AppointmentStatus { PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED }
}