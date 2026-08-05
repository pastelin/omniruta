package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request;


import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignatureVectorDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HEADCreatePrescriptionRequest(
        @NotNull Long jobId,

        @NotBlank String staffName,
        @NotBlank String patientName,
        Integer patientAge,
        String patientGender,
        String patientAddress,
        String licenceNo,
        @NotBlank String diagnosis,
        String additionalInstructions,

        @NotEmpty List<HEADCreatePrescriptionMedicationRequest> medications,

        @NotNull HEADSignatureVectorDto signature
) {}


