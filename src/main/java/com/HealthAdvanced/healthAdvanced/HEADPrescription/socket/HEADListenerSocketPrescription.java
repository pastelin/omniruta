package com.HealthAdvanced.healthAdvanced.HEADPrescription.socket;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.service.HEADStaffRatingService;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.dto.HEADPrescriptionDraftDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADPrescriptionDraftJoinRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADPrescriptionIssueRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADErrorPrescriptionDto;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.HealthAdvanced.healthAdvanced.HEADPrescription.socket.HEADPrescriptionEvents.DESCRIPTION_OK;
import static com.HealthAdvanced.healthAdvanced.HEADPrescription.socket.HEADPrescriptionEvents.PRESCRIPTION_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADListenerSocketPrescription {

    private final HEADPrescriptionSocketHandler prescriptionSocketHandler;

    private static String uuidOf(SocketIOClient c) { return c.get("userUuid"); }

    // ===== JOIN (staff) =====
    public void onDraftJoin(SocketIOClient staffClient, HEADPrescriptionDraftJoinRequest req, AckRequest ack) {
        Optional.ofNullable(uuidOf(staffClient)).ifPresentOrElse(userUuid -> {
            try {
                prescriptionSocketHandler.onJoin(staffClient, req, userUuid);

                if (ack != null && ack.isAckRequested()) ack.sendAckData(HEADWsEnvelope.ok(DESCRIPTION_OK, HEADErrorPrescriptionDto.mapError("Se obtiene el join correctament")));
            } catch (Exception e) {
                log.error("[PRESCRIPTION_DRAFT_JOIN] staffUuid={} err={}", userUuid, e.getMessage(), e);
                staffClient.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR, HEADErrorPrescriptionDto.mapError("Error al unirse a receta")));
            }
        }, staffClient::disconnect);
    }

    // ===== UPSERT DRAFT (staff/doctor) =====
    public void onDraftUpsert(SocketIOClient staffClient, HEADPrescriptionDraftDto req, AckRequest ack) {
        Optional.ofNullable(uuidOf(staffClient)).ifPresentOrElse(doctorUuid -> {
            try {
                prescriptionSocketHandler.onUpsertDraft(staffClient, req, doctorUuid);
                if (ack != null && ack.isAckRequested()) ack.sendAckData(HEADWsEnvelope.ok(DESCRIPTION_OK, HEADErrorPrescriptionDto.mapError("Se actualiza el draft correctamente")));
            } catch (Exception e) {
                log.error("[PRESCRIPTION_DRAFT_UPSERT] staffUuid={} jobId={} err={}",
                        doctorUuid,
                        req != null ? req.jobId() : null,
                        e.getMessage(),
                        e
                );
                staffClient.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR, HEADErrorPrescriptionDto.mapError("Error al actualizar receta")));
            }
        }, staffClient::disconnect);
    }

    // ===== ISSUE (staff/doctor) =====
    public void onIssue(SocketIOClient staffClient, HEADPrescriptionIssueRequest req, AckRequest ack) {
        Optional.ofNullable(uuidOf(staffClient)).ifPresentOrElse(doctorUuid -> {
            try {
                prescriptionSocketHandler.onIssue(staffClient, req, doctorUuid);
                if (ack != null && ack.isAckRequested()) ack.sendAckData(HEADWsEnvelope.ok(DESCRIPTION_OK, HEADErrorPrescriptionDto.mapError("Se realiza la firma correctamente")));
            } catch (Exception e) {
                log.error("[PRESCRIPTION_ISSUE] staffUuid={} jobId={} err={}",
                        doctorUuid,
                        req != null ? req.jobId() : null,
                        e.getMessage(),
                        e
                );
                staffClient.sendEvent(PRESCRIPTION_ERROR, HEADWsEnvelope.ok(PRESCRIPTION_ERROR, HEADErrorPrescriptionDto.mapError("Error al emitir receta")));
            }
        }, staffClient::disconnect);
    }
}

