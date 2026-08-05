package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response;

public record HEADPrescriptionMedicationPreviewDto(
        String name,
        String dosage,
        String frequency,
        String duration,
        String instructions
) {}