package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADAckResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADJobCancelDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADJobCancelWithNoteDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADClientUpdateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobEventPublisher;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.mappers.HEADJobMapper;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobQueueStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADRideAssignmentService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request.DoctorAvailabilityRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelReason;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.ERROR_RESPONSE_EVENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADListenerSocketServerStaff {

    private final HEADRideAssignmentService rideAssignmentService;
    private final HEADJobService jobService;
    private final HEADWsEmitter emitter;
    private final HEADJobMapper jobMapper;


    public void acceptedOfferJob(String uuid, Long jobId, AckRequest ack) {

        rideAssignmentService.cancelDecisionTimeout(jobId);
        HEADJob accepted = jobService.acceptByStaff(uuid, jobId);

        // stop completo del motor
        rideAssignmentService.onStaffAccepted(jobId, uuid);

        //HEADClientUpdateDto update = jobMapper.mapToClientUpdateDto(accepted);


        emitter.emitJobAcceptedToStaff(uuid, jobId);

        if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
    }

    public void rejectedOfferJob(String uuid, Long jobId, AckRequest ack) {
        rideAssignmentService.onStaffRejected(jobId, uuid);
        if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
    }

    public void cancelJobOffer(String uuid, Long jobId, AckRequest ack) {
        rideAssignmentService.onStaffScheduleCancel(jobId,uuid);
        if (ack.isAckRequested()) {
            ack.sendAckData(HEADAckResponse.oks());
        }
        else {
            ack.sendAckData(HEADAckResponse.fail("Error al cancelar"));
        }
    }

    public void onCancelToStaff(String uuIdStaff, HEADJobCancelWithNoteDto req, AckRequest ack) {

        try {
            jobService.cancelByStaff(uuIdStaff,req.idJob(), HEADCancelReason.STAFF_CANCELLED, req.note());
            ack.sendAckData(HEADAckResponse.oks());

        } catch (HEADBadRequestException ex) {
            emitter.emitToClient(uuIdStaff, ERROR_RESPONSE_EVENT, ex.getMessage());
            ack.sendAckData(HEADAckResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            emitter.emitToClient(uuIdStaff, ERROR_RESPONSE_EVENT, ex.getMessage());
            ack.sendAckData(HEADAckResponse.fail("Error al cancelar, inténtalo más tarde: " + ex.getMessage()));
        }
    }

    public void onScheduleJob(String uuid, DoctorAvailabilityRequest request, AckRequest ack) {
        try {
            rideAssignmentService.cancelDecisionTimeout(request.jobId());
            var responseAck = jobService.scheduleOfferJob(uuid,request);
            if (responseAck.success()) {
                rideAssignmentService.onStaffSchedulePending(request.jobId(), uuid);
                ack.sendAckData(HEADAckResponse.oks());
            }
            else {
                ack.sendAckData(HEADAckResponse.fail(responseAck.messageError()));
            }
        } catch (Exception ex) {
            log.error("[STAFF_JOB_STARTED] error err={}", ex.toString(), ex);
            ack.sendAckData(HEADWsEnvelope.fail("STARTED_ERROR"));
            emitter.toUser(uuid, ERROR_RESPONSE_EVENT, ex.getMessage());
        }
    }

    public void decisionOpened(String uuid, Long jobId, AckRequest ack) {
        try {
            log.info("[DECISION_OPENED] decisionOpened jobId={}", jobId);
            rideAssignmentService.onDecisionOpened(jobId, uuid);
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
            log.warn("[DECISION_OPENED] jobId={} staffUuid={} decision window started", jobId, jobId);
        } catch (Exception ex) {
            log.error("[DECISION_OPENED] err={}", ex.toString(), ex);
            log.error("[DECISION_OPENED] jobId={} staffUuid={}", jobId, uuid);
            log.error("[DECISION_OPENED] decisionOpened jobId={} messageError{} ", jobId, ex.getMessage());
            if (ack.isAckRequested()) ack.sendAckData(HEADWsEnvelope.fail("DECISION_OPENED_ERROR"));
            emitter.toUser(uuid, ERROR_RESPONSE_EVENT, ex.getMessage());
        }
    }

}
