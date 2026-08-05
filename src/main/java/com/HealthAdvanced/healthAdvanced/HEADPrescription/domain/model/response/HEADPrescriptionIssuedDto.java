package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.dto.HEADPrescriptionMedicationDraftDto;

import java.util.List;

public record HEADPrescriptionIssuedDto(
        Long prescriptionId,
        Long jobId,
        Long issuedAtMs,
        HEADSignatureVectorDto signature,
        HEADPrescriptionPreviewDto preview
) {}

