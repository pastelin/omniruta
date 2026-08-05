package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.dto.response;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADMedicationForm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record HEADMedicationsTodayResponse(
        LocalDate date,
        int taken,
        int total,
        List<MedicationCard> medications
) {
    public record MedicationCard(
            Long medicationId,
            String name,
            String dosage,
            String emoji,
            List<Dose> doses
    ) {}

    public record Dose(
            Long doseId,
            String time,
            HEADDoseStatus status
    ) {}
}