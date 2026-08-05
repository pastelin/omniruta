package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.mapper;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPrescriptionMedicationPreviewDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPrescriptionPreviewDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignatureVectorDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescription;
import org.springframework.stereotype.Component;

@Component
public class HEADPrescriptionPreviewDtoMapper {

    public static HEADPrescriptionPreviewDto from(HEADPrescription p, HEADSignatureVectorDto signature) {
        return new HEADPrescriptionPreviewDto(
                p.getId(),
                p.getJob().getId(),

                p.getDoctorName(),
                p.getDoctorSpecialty(),
                p.getDoctorLicenseNo(),

                p.getPatientName(),
                p.getPatientAge(),
                p.getPatientGender(),
                p.getPatientAddress(),
                p.getDateText(),

                p.getDiagnosis(),
                p.getAdditionalInstructions(),

                p.getMedications().stream()
                        .map(m -> new HEADPrescriptionMedicationPreviewDto(
                                m.getName(),
                                m.getDosage(),
                                m.getFrequency(),
                                m.getDuration(),
                                m.getInstructions()
                        ))
                        .toList(),

                signature,
                p.getIssuedAt() != null ? p.getIssuedAt().toEpochMilli() : null
        );
    }
}