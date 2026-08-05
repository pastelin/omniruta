package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.dto;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPrescriptionIssuedDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignatureVectorDto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record HEADPrescriptionDraftDto(
        Long jobId,
        String clientUuid,
        String doctorUuid,

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

        List<String> symptoms,
        LocalDate followUpDate,
        String notes,

        List<HEADPrescriptionMedicationDraftDto> medications,

        Instant createdAt,
        Long version,
        long updatedAtMs,
        Instant startedService,
        HEADPrescriptionIssuedDto issued
) {}
