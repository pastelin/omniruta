package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADFrequencyMode;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADMedicationForm;
import jakarta.validation.constraints.NotBlank;

public record HEADCreatePrescriptionMedicationRequest(
        @NotBlank String name,
        String dosage,
        String frequency,
        String duration,
        String instructions,
        Integer durationDays,
        HEADFrequencyMode frequencyMode,
        Integer timesPerDay,
        Integer intervalHours,
        HEADMedicationForm medFormCode
) {}