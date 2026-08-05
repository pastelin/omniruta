package com.HealthAdvanced.healthAdvanced.HEADPrescription.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPrescriptionDataResponse;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescriptionMedication;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.repositories.HEADPrescriptionJpaRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class HEADPrescriptionDetailService {

    private final HEADPrescriptionJpaRepository repo;
    private final HEADJwtGenerator jwt;
    private final HEADPrescriptionService prescriptionService;
    private final HEADFileAssetRepository fileAssetRepository;
    private final HEADPersonalUserRepository headPersonalUserRepository;


    private static final ZoneId ZONE = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter DATE_TEXT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es-MX")).withZone(ZONE);

    @Transactional
    public HEADPrescriptionDataResponse getById(Long prescriptionId) {
        String clientUuid = jwt.getUserNamePersonalUser();

        var p = repo.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));

        if (!clientUuid.equals(p.getClientUuid())) {
            throw new IllegalArgumentException("Not authorized");
        }

        var getStaffCurrent = headPersonalUserRepository.findByUidUser(p.getDoctorUuid()).orElse(null);
        var avatarStaff = "";
        if (getStaffCurrent != null) {
            var getAvatarUrl = fileAssetRepository.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.STAFF, getStaffCurrent.getIdUser(), HEADCategory.AVATAR).orElse(null);
            avatarStaff = getAvatarUrl != null ? getAvatarUrl.getUrl() : "";
        }
        // ---- Doctor ----
        var doctor = new HEADPrescriptionDataResponse.Doctor(
                safe(p.getDoctorName()),
                safe(p.getDoctorSpecialty()),
                safe(p.getDoctorLicenseNo()),
                avatarStaff
        );

        // ---- Patient ----
        String ageGender = buildAgeGender(p.getPatientAge(), p.getPatientGender());

        String dateText = safe(p.getDateText());
        if (dateText.isBlank()) {
            Instant issued = p.getIssuedAt();
            dateText = (issued != null) ? DATE_TEXT_FMT.format(issued) : "";
        }

        var patient = new HEADPrescriptionDataResponse.Patient(
                safe(p.getPatientName()),
                ageGender,
                dateText,
                safe(p.getPatientAddress())
        );

        // ---- Medications ----
        List<HEADPrescriptionDataResponse.Medication> meds =
                (p.getMedications() == null ? List.of()
                        : p.getMedications().stream()
                        .map(m -> new HEADPrescriptionDataResponse.Medication(
                                safe(m.getName()),
                                safe(m.getDosage()),
                                buildFrequencyText(m),
                                buildDurationText(m),
                                safe(m.getInstructions())
                        ))
                        .toList()
                );

        // ---- Signature ----
        var vector = prescriptionService.fromJson(p.getSignatureVectorJson());
        var symptoms = prescriptionService.readSymptoms(p.getSymptomsJson());

        // Si no tienes estos campos guardados, usamos doctor como firma.
        String signatureName = safe(p.getDoctorName());
        String signatureTitle = !safe(p.getDoctorSpecialty()).isBlank()
                ? safe(p.getDoctorSpecialty())
                : "Médico";

        return new HEADPrescriptionDataResponse(
                p.getJob().getId(),
                p.getPrescriptionCode(),
                doctor,
                patient,
                safe(p.getDiagnosis()),
                symptoms,
                p.getFollowUpDate(),
                p.getNotes(),
                meds,
                safe(p.getAdditionalInstructions()),
                signatureName,
                signatureTitle,
                vector
        );
    }

    private String buildAgeGender(Integer age, String gender) {
        String a = (age == null) ? "" : age + " años";
        String g = safe(gender);
        if (!a.isBlank() && !g.isBlank()) return a + " • " + g;
        return (a + (g.isBlank() ? "" : " " + g)).trim();
    }

    private String buildFrequencyText(HEADPrescriptionMedication m) {
        if (m.getFrequencyMode() != null) {
            return switch (m.getFrequencyMode()) {
                case TIMES_PER_DAY -> {
                    Integer t = m.getTimesPerDay();
                    yield (t == null) ? "1 vez al día" : (t + (t == 1 ? " vez al día" : " veces al día"));
                }
                case INTERVAL_HOURS -> {
                    Integer h = m.getIntervalHours();
                    yield (h == null) ? "Cada 24 horas" : ("Cada " + h + " horas");
                }
            };
        }
        return safe(m.getFrequency());
    }

    private String buildDurationText(HEADPrescriptionMedication m) {
        if (m.getDurationDays() != null) {
            int d = Math.max(1, m.getDurationDays());
            return d + (d == 1 ? " día" : " días");
        }
        return safe(m.getDuration());
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}

