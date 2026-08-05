package com.HealthAdvanced.healthAdvanced.HEADPrescription.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.service.HEADMedicationTrackingService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.application.HEADPrescriptionCodeGenerator;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADPrescriptionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.dto.HEADPrescriptionDraftDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADCreatePrescriptionRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignatureVectorDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescription;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescriptionMedication;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.repositories.HEADPrescriptionJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HEADPrescriptionService {

    private final HEADPrescriptionJpaRepository prescriptionRepo;
    private final HEADJobRepository headJobRepo; // AJUSTA NOMBRE
    private final ObjectMapper objectMapper;
    private final HEADPrescriptionCodeGenerator codeGenerator;
    private final ObjectMapper om;
    private final HEADMedicationTrackingService headMedicationTrackingService;
    private final HEADOccupationPersonalUserRepository headOccupationPersonalUserRepository;

    @Transactional
    public HEADPrescription issuePrescription(HEADCreatePrescriptionRequest req, HEADPrescriptionDraftDto draft, String doctorUuidFromSession) {
        // 1) cargar job
        var job = headJobRepo.findById(req.jobId())
                .orElseThrow(() -> new HEADBadRequestException("Job not found: " + req.jobId()));

        // 2) validar que haya staff asignado
        if (job.getStaffUuid() == null || job.getStaffUuid().isBlank()) {
            throw new HEADBadRequestException("El trabajo aún no tiene staff asignado");
        }

        // 3) validar que el emisor sea el staff/doctor del job
        if (!doctorUuidFromSession.equals(job.getStaffUuid())) {
            throw new HEADBadRequestException("No autorizado para emitir receta en este trabajo");
        }

        // 4) evitar duplicado (1 receta por job)
        if (prescriptionRepo.existsByJob_Id(req.jobId())) {
            throw new HEADBadRequestException("Este trabajo ya tiene una receta emitida");
        }

        // 5) obtener clientUuid desde el job (no del request)
        // AJUSTA getter: en tu HEADClients debe existir algo tipo getUuIdUser()
        String clientUuid = job.getClient().getUuIdUser();

        // 6) construir entidad
        var p = new HEADPrescription();
        p.setPrescriptionCode(codeGenerator.nextCode());
        p.setJob(job);
        p.setClientUuid(clientUuid);
        p.setDoctorUuid(doctorUuidFromSession);

        // ---- Doctor snapshot (recomendado: leer de HEADPersonalUser o tu perfil doctor) ----
        // Si job.getStaffUser() existe y tiene nombre/cédula, úsalo aquí.
        // MVP: mínimo doctorName requerido por tu UI: llénalo con lo que tengas
        p.setDoctorName(req.staffName());
        p.setDoctorSpecialty(draft.doctorSpecialty());
        p.setDoctorLicenseNo(draft.doctorLicenseNo()); // <-- cédula profesional si la tienes en tu tabla
        p.setDoctorClinicName(null);
        p.setDoctorClinicAddress(null);
        p.setDoctorPhone(null);
        p.setDoctorEmail(null);
        p.setCreatedAt(draft.createdAt());
        p.setSymptomsJson(writeJson(draft.symptoms()));
        p.setFollowUpDate(draft.followUpDate());
        p.setNotes(draft.notes());
        // ---- Patient snapshot ----
        p.setPatientName(req.patientName());
        p.setPatientAge(req.patientAge());
        p.setPatientGender(req.patientGender());
        p.setPatientAddress(req.patientAddress());
        p.setDateText(draft.dateText());

        // ---- Content ----
        p.setDiagnosis(req.diagnosis());
        p.setAdditionalInstructions(req.additionalInstructions());

        // ---- Signature ----
        p.setSignatureVectorJson(writeJson(req.signature()));
        p.setSignatureSignedAt(Instant.now());

        p.setStatus(HEADPrescriptionStatus.ISSUED);
        p.setIssuedAt(Instant.now());

        // 7) meds con lineNo
        int line = 1;
        for (var mreq : req.medications()) {
            var m = new HEADPrescriptionMedication();
            m.setLineNo(line++);
            m.setName(mreq.name());
            m.setDosage(mreq.dosage());
            m.setFrequency(mreq.frequency());
            m.setDuration(mreq.duration());
            m.setInstructions(mreq.instructions());
            m.setDurationDays(mreq.durationDays());
            m.setFrequencyMode(mreq.frequencyMode());
            m.setTimesPerDay(mreq.timesPerDay());
            m.setIntervalHours(mreq.intervalHours());
            m.setMedForm(mreq.medFormCode());
            p.addMedication(m);
        }


        var saved = prescriptionRepo.save(p);
        headMedicationTrackingService.onPrescriptionIssued(saved);
        return saved;
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new HEADBusinessException("No se pudo serializar firma: " + e.getMessage());
        }
    }

    public String toJson(HEADSignatureVectorDto v) {
        if (v == null) return null;
        try {
            return om.writeValueAsString(v);
        } catch (JsonProcessingException e) {
            throw new HEADBusinessException("Invalid signature vector (serialize): " + e.getMessage());
        }
    }

    public HEADSignatureVectorDto fromJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return om.readValue(json, HEADSignatureVectorDto.class);
        } catch (Exception e) {
            throw new HEADBusinessException("Invalid signature vector json (deserialize): " + e.getMessage());
        }
    }

    public List<String> readSymptoms(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new HEADBusinessException("No se pudo deserializar symptoms: " + e.getMessage());
        }
    }
}
