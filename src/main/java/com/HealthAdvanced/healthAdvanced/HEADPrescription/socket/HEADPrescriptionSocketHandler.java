package com.HealthAdvanced.healthAdvanced.HEADPrescription.socket;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff.HEADNameFormatters;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.mapper.HEADPrescriptionPreviewDtoMapper;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.dto.HEADPrescriptionDraftDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADCreatePrescriptionMedicationRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADCreatePrescriptionRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADPrescriptionDraftJoinRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADPrescriptionIssueRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADErrorPrescriptionDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPrescriptionIssuedDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignatureVectorDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescription;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.repositories.HEADPrescriptionJpaRepository;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.redis.HEADPrescriptionDraftRedisStore;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.service.HEADPrescriptionService;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADStaffCredentialRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.HealthAdvanced.healthAdvanced.HEADPrescription.socket.HEADPrescriptionEvents.*;
import static java.time.Instant.now;


@Component
@RequiredArgsConstructor
public class HEADPrescriptionSocketHandler {

    private final HEADWsEmitter emitter;
    private final HEADPrescriptionDraftRedisStore draftStore;
    private final HEADPrescriptionService prescriptionService;
    private final HEADJobRepository headJobRepo;
    private final HEADOccupationPersonalUserRepository headOccupationPersonalUserRepository;
    private final HEADPersonalUserRepository headPersonalUserRepository;
    private final HEADClientsRepository headClientsRepository;
    private final HEADStaffCredentialRepository credentialNoRepo;
    private final HEADPrescriptionJpaRepository prescriptionRepo;

    /** Sirve para que al abrir la pantalla el cliente/doctor pida el último draft. */
    public void onJoin(SocketIOClient c, HEADPrescriptionDraftJoinRequest req, String uuidFromSession) {
        var view = headJobRepo.findJobUuids(req.jobId())
                .orElseThrow(() -> new HEADBadRequestException("Job not found: " + req.jobId()));

        String clientUuid = view.getClientUuid();
        String staffUuid  = view.getStaffUuid();

        if (staffUuid == null || staffUuid.isBlank()) {
            c.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR,
                    HEADErrorPrescriptionDto.mapError("Este trabajo no tiene staff asignado")));
            return;
        }

        HEADClients getClient = headClientsRepository.findByUuIdUser(clientUuid)
                .orElseThrow(() -> new HEADBadRequestException("Client not found: " + clientUuid));
        HEADPersonalUser getPersonal = headPersonalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Personal not found: " + staffUuid));

        var specialityName = headOccupationPersonalUserRepository.findOccupationLabelByStaffUserId(getPersonal.getIdUser()).stream().findFirst().orElse(null);

        if (specialityName == null) {
            specialityName = headOccupationPersonalUserRepository.findPrimaryOccupationCodeOrNull(getPersonal.getIdUser()).getLabelEs();
        }

        if (!uuidFromSession.equals(staffUuid)) {
            c.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR,
                    HEADErrorPrescriptionDto.mapError("No autorizado para este trabajo")));
            return;
        }

        var prescription = prescriptionRepo.findByJobIdAndClientUuidWithMeds(req.jobId(),view.getClientUuid()).orElse(null);
        HEADPrescriptionIssuedDto signature = null;
        if (prescription != null) {
            var vector = prescriptionService.fromJson(prescription.getSignatureVectorJson());
            signature = issuedPrescription(prescription, req.jobId(), vector);
        }
        var occCode = headOccupationPersonalUserRepository.findPrimaryOccupationCodeOrNull(getPersonal.getIdUser());
        var nameStaff = HEADNameFormatters.buildStaffDisplayName(getPersonal, occCode);
        var getCredentialNo = credentialNoRepo.findApprovedGlobalLicenseNo(getPersonal.getIdUser()).orElse(null);
        var nameClient = getClient.getNombre() + " " + getClient.getAPaterno();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.of("America/Mexico_City"));
        String dateText = fmt.format(now());

        var draft = draftStore.get(req.jobId());
        if (draft == null) {
            draft = new HEADPrescriptionDraftDto(
                    req.jobId(),
                    clientUuid,
                    staffUuid,

                    nameStaff,
                    specialityName,
                    getCredentialNo,

                    nameClient,
                    null,
                    "",
                    "",
                    dateText,

                    "",
                    "",
                    List.of(),
                    null,
                    null,
                    List.of(),
                    now(),
                    1L,
                    System.currentTimeMillis(),
                    view.getStartedAt(),
                    signature

            );
            draftStore.put(req.jobId(), draft);
        }
        emitter.emitToClient(clientUuid, PRESCRIPTION_DRAFT_UPDATED, draft);
        c.sendEvent(PRESCRIPTION_DRAFT_UPDATED, HEADWsEnvelope.ok(PRESCRIPTION_DRAFT_UPDATED, draft));
    }


    /** Doctor actualiza en tiempo real (draft completo). */
    public void onUpsertDraft(SocketIOClient c, HEADPrescriptionDraftDto incoming, String doctorUuidFromSession) {
        var view = headJobRepo.findJobUuids(incoming.jobId())
                .orElseThrow(() -> new HEADBadRequestException("Job not found: " + incoming.jobId()));

        String clientUuid = view.getClientUuid();
        String staffUuid  = view.getStaffUuid();

        var prescription = prescriptionRepo.findByJobIdAndClientUuidWithMeds(incoming.jobId(),view.getClientUuid()).orElse(null);
        HEADPrescriptionIssuedDto signature = null;
        if (prescription != null) {
            var vector = prescriptionService.fromJson(prescription.getSignatureVectorJson());
            signature = issuedPrescription(prescription, incoming.jobId(), vector);
        }

        if (staffUuid == null || staffUuid.isBlank()) {
            c.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR,
                    HEADErrorPrescriptionDto.mapError("Este trabajo no tiene staff asignado")));
            return;
        }

        HEADClients getClient = headClientsRepository.findByUuIdUser(clientUuid)
                .orElseThrow(() -> new HEADBadRequestException("Client not found: " + clientUuid));
        HEADPersonalUser getPersonal = headPersonalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Personal not found: " + staffUuid));

        var specialityName = headOccupationPersonalUserRepository.findOccupationLabelByStaffUserId(getPersonal.getIdUser()).stream().findFirst().orElse(null);

        if (specialityName == null) {
            specialityName = headOccupationPersonalUserRepository.findPrimaryOccupationCodeOrNull(getPersonal.getIdUser()).getLabelEs();
        }

        if (!doctorUuidFromSession.equals(staffUuid)) {
            c.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR,
                    HEADErrorPrescriptionDto.mapError("No autorizado para este trabajo")));
            return;
        }

        var occCode = headOccupationPersonalUserRepository.findPrimaryOccupationCodeOrNull(getPersonal.getIdUser());
        var nameStaff = HEADNameFormatters.buildStaffDisplayName(getPersonal, occCode);
        var getCredentialNo = credentialNoRepo.findApprovedGlobalLicenseNo(getPersonal.getIdUser()).orElse(null);

        var prev = draftStore.get(incoming.jobId());
        long nextVersion = (prev == null ? 1 : prev.version() + 1);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.of("America/Mexico_City"));
        String dateText = fmt.format(now());
        String dateNow = incoming.dateText() == null ? dateText : incoming.dateText();

        var merged = new HEADPrescriptionDraftDto(
                incoming.jobId(),
                clientUuid,
                staffUuid,

                nameStaff,
                specialityName,
                getCredentialNo,

                incoming.patientName(),
                incoming.patientAge(),
                incoming.patientGender(),
                incoming.patientAddress(),
                dateNow.isBlank() ? dateText : dateNow,

                incoming.diagnosis(),
                incoming.additionalInstructions(),
                incoming.symptoms(),
                incoming.followUpDate(),
                incoming.notes(),
                incoming.medications(),

                prev != null ? prev.createdAt() : now(),
                nextVersion,
                System.currentTimeMillis(),
                view.getStartedAt(),
                signature
        );

        draftStore.put(incoming.jobId(), merged);

        emitter.emitToClient(clientUuid, PRESCRIPTION_DRAFT_UPDATED, merged);

        // (opcional) realtime al doctor por uuid (por si usa 2 dispositivos)
        emitter.toUser(staffUuid, PRESCRIPTION_DRAFT_UPDATED, merged);
    }

    /** Doctor firma y emite: persiste en MySQL y notifica final. */
    public void onIssue(SocketIOClient c, HEADPrescriptionIssueRequest req, String doctorUuidFromSession) {
        if (req == null || req.jobId() == null) {
            c.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR,HEADErrorPrescriptionDto.mapError("jobId requerido")));
            return;
        }
        if (req.signature() == null) {
            c.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR,HEADErrorPrescriptionDto.mapError("Firma requerida")));
            return;
        }

        var view = headJobRepo.findJobUuids(req.jobId())
                .orElseThrow(() -> new HEADBadRequestException("Job not found: " + req.jobId()));

        String clientUuid = view.getClientUuid();
        String staffUuid  = view.getStaffUuid();

        if (staffUuid == null || staffUuid.isBlank()) {
            c.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR,
                    HEADErrorPrescriptionDto.mapError("Este trabajo no tiene staff asignado")));
            return;
        }

        if (!doctorUuidFromSession.equals(staffUuid)) {
            c.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR,
                    HEADErrorPrescriptionDto.mapError("No autorizado para este trabajo")));
            return;
        }

        var draft = draftStore.get(req.jobId());
        if (draft == null) {
            c.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR,HEADErrorPrescriptionDto.mapError("No hay borrador para emitir")));
            return;
        }

        var createReq = new HEADCreatePrescriptionRequest(
                draft.jobId(),
                req.nameStaff(),
                draft.patientName(),
                draft.patientAge(),
                draft.patientGender(),
                draft.patientAddress(),
                draft.doctorLicenseNo(),
                draft.diagnosis(),
                draft.additionalInstructions(),
                draft.medications().stream()
                        .map(m -> new HEADCreatePrescriptionMedicationRequest(
                                m.name(), m.dosage(), m.frequency(), m.duration(), m.instructions(),
                                m.durationDays(), m.frequencyMode(), m.timesPerDay(), m.intervalHours(), m.medFormCode()
                        ))
                        .toList(),

                req.signature()
        );

        var saved = prescriptionService.issuePrescription(createReq, draft, doctorUuidFromSession);

        var issued = issuedPrescription(saved, req.jobId(),req.signature());

        emitter.emitToClient(clientUuid, PRESCRIPTION_ISSUED, issued);
        emitter.toUser(staffUuid, PRESCRIPTION_ISSUED, issued);
        emitter.emitToClient(clientUuid, PRESCRIPTION_DRAFT_UPDATED, draft);
        emitter.toUser(staffUuid, PRESCRIPTION_DRAFT_UPDATED, draft);
    }

    private HEADPrescriptionIssuedDto issuedPrescription(HEADPrescription headPrescription, Long jobId, HEADSignatureVectorDto signature) {
        var preview = HEADPrescriptionPreviewDtoMapper.from(headPrescription, signature);
        return new HEADPrescriptionIssuedDto(
                headPrescription.getId(),
               jobId,
                System.currentTimeMillis(),
               signature,
                preview
        );
    }
}

