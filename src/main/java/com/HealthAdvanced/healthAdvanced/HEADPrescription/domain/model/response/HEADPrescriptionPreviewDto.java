package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response;

import java.util.List;

public record HEADPrescriptionPreviewDto(
        Long prescriptionId,
        Long jobId,

        String doctorName,
        String doctorSpecialty,
        String doctorLicenseNo,

        String patientName,
        Integer patientAge,
        String patientGender,
        String patientAddress,
        String dateText,

        String diagnosis,
        String additionalInstructions,

        List<HEADPrescriptionMedicationPreviewDto> medications,

        HEADSignatureVectorDto signature,
        Long issuedAtMs
) {}