package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.dto.response;

import java.time.LocalDate;
import java.util.List;

public record HEADMedicationsDashboardResponse(
        LocalDate date,
        int progressPercentToday,
        int takenToday,
        int totalToday,
        NextDose nextDose,
        int streakDays,
        int adherencePercent30d
) {
    public record NextDose(
            Long doseId,
            Long medicationId,
            String medicationName,
            String dosage,
            String emoji,
            String time,        // "16:00"
            long minutesUntil   // ej. 150
    ) {}
}