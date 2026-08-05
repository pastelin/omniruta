package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.dto;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADFrequencyMode;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADMedicationForm;

import java.time.Instant;

public record HEADPrescriptionMedicationDraftDto(
        String name,
        String dosage,
        String frequency,
        String duration,
        String instructions,
        Integer durationDays,
        Integer timesPerDay,
        Integer intervalHours,
        HEADFrequencyMode frequencyMode,
        HEADMedicationForm medFormCode,
        Long addedAt
) {}