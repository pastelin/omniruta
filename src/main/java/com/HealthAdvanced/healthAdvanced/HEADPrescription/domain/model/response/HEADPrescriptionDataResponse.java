package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response;

import java.time.LocalDate;
import java.util.List;

public record HEADPrescriptionDataResponse(
        Long jobId,
        String folio,
        Doctor doctor,
        Patient patient,
        String diagnosis,
        List<String> symptoms,
        LocalDate followUpDate,
        String notes,
        List<Medication> medications,
        String additionalInstructions,
        String signatureName,
        String signatureTitle,
        HEADSignatureVectorDto signatureVector
) {
    public record Doctor(String name, String specialty, String licenseNo, String urlAvatar) {}
    public record Patient(String name, String ageGender, String dateText, String address) {}
    public record Medication(String name, String dosage, String frequency, String duration, String instructions) {}
}