package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADAckResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.implementations.HEADListenersSocket;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADUserDirectory;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.HEADChatSendMessageRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse.HEADChatPresenceDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventsListeners.HEADEventsListenersSocket;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallContextType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallEndReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.request.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket.HEADCallRoutingStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket.HEADCallSignalingHandler;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobQueryService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request.DoctorAvailabilityRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request.HEADStaffScheduleProposeRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.response.HEADErrorAckEvent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.api.HEADListenerSocketServerStaffRating;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.dto.HEADPrescriptionDraftDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADPrescriptionDraftJoinRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADPrescriptionIssueRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.socket.HEADListenerSocketPrescription;
import com.corundumstudio.socketio.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.*;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADEventsStaffConst.*;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventsName.HEADChatWsEvents.*;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventsName.HEADChatWsEvents.CHAT_TYPING;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket.HEADCallSocketEvents.*;
import static com.HealthAdvanced.healthAdvanced.HEADPrescription.socket.HEADPrescriptionEvents.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADStaffWebSocketListener {

    private final SocketIOServer server;
    private SocketIONamespace ns; // /staff
    private final HEADJwtGenerator jwt;
    private final HEADPresenceStore presence;
    private final HEADStaffStateStore staffState;
    private final HEADJobQueryService jobQuery;
    private final HEADUserDirectory userDirectory;
    private final HEADWsEmitter emitter;
    private final HEADListenerSocketServerStaff listenerSocketServerStaff;
    private final HEADJobService jobService;
    private final HEADEventsListenersSocket headEventsListenersSocket;
    private final HEADCallSignalingHandler callHandler;
    private final HEADCallRoutingStore callRouting;
    private final HEADListenerSocketServerStaffRating staffRatingService;
    private final HEADListenerSocketPrescription listenerSocketPrescription;

    private final AtomicBoolean registered = new AtomicBoolean(false);

    @PostConstruct
    void init() {
        this.ns = server.addNamespace(NS_PATH);
        var listeners = new HEADListenersSocket(ns, new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        );

        ns.addConnectListener(this::onConnect);
        ns.addDisconnectListener(this::onDisconnect);

        // heartbeats / reauth
        ns.addEventListener(STAFF_HEARTBEAT, String.class, this::onHeartbeat);
        ns.addEventListener(STAFF_REAUTH, String.class, this::onReauth);

        // estado / ubicación
        listeners.onTx(STAFF_ONLINE_TOGGLE, HEADStaffOnlineToggleDto.class, this::onOnlineToggle);
        listeners.onTx(STAFF_LOC_UPDATE, HEADLocUpdateDto.class, this::onLocUpdate);
        listeners.onTx(JOB_ACCEPT, HEADAcceptDto.class, this::onAccepted);
        listeners.onTx(STAFF_SCHEDULE_PENDING, DoctorAvailabilityRequest.class, this::onScheduleJob);
        listeners.onTx(STAFF_OFFER_SCHEDULE_PROPOSE, HEADStaffScheduleProposeMultiRequest.class, this::onSchedulePropose);
        listeners.onTx(DECISION_OPENED, HEADJobIdDto.class, this::onDecisionOpened);
        listeners.onTx(JOB_ARRIVED, HEADJobArrivedDto.class, this::onArrived);
        listeners.onTx(JOB_STARTED, HEADJobStartedDto.class, this::onStarted);
        listeners.onTx(JOB_COMPLETED, HEADJobCompleteDto.class, this::onCompleted);
        listeners.onTx(JOB_CANCEL, HEADJobCancelDto.class, this::onCancelJob);
        listeners.onTx(JOB_CANCEL_STAFF, HEADJobCancelWithNoteDto.class, this::onCancelStaff);
        listeners.onTx(JOB_STARTED_PIN, HEADJobStartedWithPinDto.class, this::onStartedWithPin);

        //--------- chat ----------
        listeners.onTx(CHAT_SEND_MESSAGE, HEADChatSendMessageRequest.class, headEventsListenersSocket::onSendMessage);
        listeners.onTx(CHAT_MARK_DELIVERED, HEADChatMarkDeliveredRequest.class, headEventsListenersSocket::onMarkDelivered);
        listeners.onTx(CHAT_MARK_READ, HEADChatMarkReadRequest.class, headEventsListenersSocket::onMarkRead);
        listeners.onTx(CHAT_HISTORY_REQUEST, HEADChatHistoryRequest.class, headEventsListenersSocket::onHistory);
        listeners.onTx(CHAT_UNREAD_SUMMARY_REQ, Boolean.class ,headEventsListenersSocket::onUnreadSummary);
        listeners.onTx(CHAT_TYPING, HEADChatTypingRequest.class, headEventsListenersSocket::onTyping);
        listeners.onTx(CHAT_ACTIVE, HEADActiveUserChayRequest.class,headEventsListenersSocket::onActiveUserChat);
        listeners.onTx(CHAT_INACTIVE, HEADChatActiveConversationClearRequest.class, headEventsListenersSocket::onInactiveUser);
        listeners.onTx(PRESENCE_UPDATE, HEADChatPresenceDto.class, headEventsListenersSocket::onPresenceUpdate);
        registerCallListeners(listeners, HEADChatParticipantType.STAFF);
        listeners.onTx(STAFF_RATING_SUMMARY_REQ,Boolean.class, this::onRateStaff);
        listeners.onTx(PRESCRIPTION_DRAFT_JOIN, HEADPrescriptionDraftJoinRequest.class, listenerSocketPrescription::onDraftJoin);
        listeners.onTx(PRESCRIPTION_DRAFT_UPSERT, HEADPrescriptionDraftDto.class, listenerSocketPrescription::onDraftUpsert);
        listeners.onTx(PRESCRIPTION_ISSUE, HEADPrescriptionIssueRequest.class, listenerSocketPrescription::onIssue);


    }

    // ---------- helpers ----------
    public static String staffRoom(String uuid) { return STAFF_ROOM_PREFIX.concat(uuid); }

    @SuppressWarnings("unchecked")
    private static List<String> rolesFrom(Map<String, Object> claims) {
        Object r = claims.get("roles");
        return (r instanceof List) ? (List<String>) r : List.of();
    }

    private static String uuidOf(SocketIOClient c) { return c.get("userUuid"); }

    private void joinActiveJobRooms(SocketIOClient c, String staffUuid) {
        Optional.ofNullable(staffUuid)
                .map(userDirectory::findStaffUserIdByUuid)
                .ifPresent(staffUserId -> jobQuery.findActiveJobIdsForStaffUserId(staffUserId)
                        .forEach(jobId -> emitter.addClientToJob(c, jobId)));
    }

    // ---------- connect/disconnect ----------
    private void onConnect(SocketIOClient c) {
        Optional.ofNullable(c.getHandshakeData().getSingleUrlParam("token"))
                .filter(t -> !t.isBlank())
                .ifPresentOrElse(token -> {
                    try {
                        var claims = jwt.extractAllClaims(token);
                        var uuid = claims.getSubject();
                        if (uuid == null || uuid.isBlank()) { c.disconnect(); return; }

                        c.set("userUuid", uuid);
                        c.joinRoom(staffRoom(uuid));
                        presence.add(c.getSessionId().toString(), uuid);
                        staffState.setAppActive(uuid,true);
                        Optional.ofNullable(staffState.get(uuid))
                                .ifPresent(state -> c.sendEvent(STAFF_STATE, state));
                        joinActiveJobRooms(c, uuid);
                        onReconnected(uuid);
                        c.sendEvent("DEBUG_PING", "hola_desde_backend");
                    } catch (Exception e) {
                        log.info("onConnect Exception  e={}", e.getMessage());
                        c.disconnect();
                    }
                }, c::disconnect);
    }

    private void onDisconnect(SocketIOClient c) {
        presence.remove(c.getSessionId().toString());
        String uuid = uuidOf(c);
        if (uuid != null) {
            boolean stillOnline = presence.isOnline(uuid);
            if (!stillOnline) staffState.setAppActive(uuid, false);
            callRouting.activeCallOf(uuid).ifPresent(callId -> {
                callHandler.forceEnd(callId, HEADCallEndReason.NETWORK);
            });
        }
    }

    // ---------- auth/heartbeat ----------
    private void onHeartbeat(SocketIOClient c, String _payload, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            presence.renew(c.getSessionId().toString());
            staffState.heartbeat(uuid);
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
        }, c::disconnect);
    }

    private void onReauth(SocketIOClient c, String newToken, AckRequest ack) {
        try {
            var claims = jwt.extractAllClaims(newToken);
            var isExpireToken = jwt.isTokenExpired(newToken);
            var newUuid = claims.getSubject();
            if (isExpireToken) { ack.sendAckData(HEADAckResponse.fail("TOKEN_EXPIRED")); return;}
            if (newUuid == null || newUuid.isBlank()) { c.disconnect(); return; }

            Optional.ofNullable(uuidOf(c)).filter(old -> !old.equals(newUuid))
                    .ifPresent(old -> c.leaveRoom(staffRoom(old)));

            c.set("userUuid", newUuid);
            c.joinRoom(staffRoom(newUuid));
            presence.update(c.getSessionId().toString(), newUuid);
            Optional.ofNullable(staffState.get(newUuid))
                    .ifPresent(state -> c.sendEvent(STAFF_STATE, state));
            joinActiveJobRooms(c, newUuid);
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            log.info("onReauth ExceptionExpired string='{}' ex={}", ex.getMessage(), ex.toString());
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.fail("TOKEN_EXPIRED"));
        } catch (Exception e) {
            log.info("onReauth Exception string='{}' e={}", e.getMessage(), e.toString());
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.fail(e.getMessage()));
        }
    }

    // ---------- estado/ubicación ----------
    private void onOnlineToggle(SocketIOClient c, HEADStaffOnlineToggleDto dto, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            presence.renew(c.getSessionId().toString());
            var newState = staffState.setOnline(uuid, dto.online());
            ns.getRoomOperations(staffRoom(uuid)).sendEvent(STAFF_STATE, newState);
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
        }, c::disconnect);
    }

    private void onLocUpdate(SocketIOClient c, HEADLocUpdateDto dto, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            presence.renew(c.getSessionId().toString());
            staffState.updateLocation(uuid, dto.lat(), dto.lng());
            jobService.refreshRouteIfNeeded(uuid, dto.lat(), dto.lng());
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
        }, c::disconnect);
    }

    private void onAccepted(SocketIOClient c, HEADAcceptDto idJob, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            presence.renew(c.getSessionId().toString());
            listenerSocketServerStaff.acceptedOfferJob(uuid,idJob.jobId(), ack);
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
        }, c::disconnect);
    }

    private void onArrived(SocketIOClient c, HEADJobArrivedDto dto, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
                try {
                    presence.renew(c.getSessionId().toString());
                    jobService.markArrived(dto.jobId(), uuid);
                    ack.sendAckData(HEADAckResponse.oks());
                } catch (Exception ex) {
                    log.error("[STAFF_JOB_ARRIVED] error jobId={} err={}", dto.jobId(), ex.getMessage(), ex);
                    ack.sendAckData(HEADAckResponse.fail(ex.getMessage()));
                }
        }, c::disconnect);
    }

    private void onStarted(SocketIOClient c, HEADJobStartedDto dto, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(staffUuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                jobService.markStarted(dto.jobId(), staffUuid); // solo VIDEO
                ack.sendAckData(HEADAckResponse.oks());
            } catch (Exception ex) {
                log.error("[STAFF_JOB_STARTED] error jobId={} err={}", dto.jobId(), ex.getMessage(), ex);
                ack.sendAckData(HEADAckResponse.fail(ex.getMessage()));
            }
        }, c::disconnect);
    }


    private void onStartedWithPin(SocketIOClient c, HEADJobStartedWithPinDto dto, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(staffUuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                jobService.markStartedWithPin(dto.jobId(), staffUuid, dto.pin());
                ack.sendAckData(HEADAckResponse.oks());
            } catch (Exception ex) {
                log.error("[STAFF_JOB_STARTED_PIN] error jobId={} err={}", dto.jobId(), ex.getMessage(), ex);
                ack.sendAckData(HEADAckResponse.fail(ex.getMessage()));
                emitter.toUser(staffUuid, ERROR_RESPONSE_EVENT, ex.getMessage());
            }
        }, c::disconnect);
    }


    private void onCompleted(SocketIOClient c, HEADJobCompleteDto dto, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                jobService.markCompletedByStaff(dto.jobId(), dto.isIssue(), uuid);
            } catch (HEADBadRequestException ex) {
                emitter.toUser(uuid, ERROR_RESPONSE_EVENT, ex.getMessage());
            }
        }, c::disconnect);
    }

    private void registerCallListeners(HEADListenersSocket listeners, HEADChatParticipantType fromType) {

        listeners.onTx(CALL_SDP, HEADCallSdpDto.class, callHandler::onSdp);
        listeners.onTx(CALL_ICE_CANDIDATE, HEADCallIceCandidateDto.class, callHandler::onIce);

        listeners.onTx(CALL_END, HEADCallEndRequest.class, callHandler::onEnd);
    }

    public void onRateStaff(SocketIOClient client,Boolean req,AckRequest ack) {
        presence.renew(client.getSessionId().toString());
        staffRatingService.onGetMyRatingSummary(client,ack);
    }

    public void onScheduleJob(SocketIOClient c, DoctorAvailabilityRequest request, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            presence.renew(c.getSessionId().toString());
            listenerSocketServerStaff.onScheduleJob(uuid,request,ack);
        }, c::disconnect);
    }

    private void onRejected(SocketIOClient c, HEADJobCancelDto req, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                listenerSocketServerStaff.rejectedOfferJob(uuid, req.idJob(), ack);
            } catch (Exception ex) {
                emitter.toUser(uuid, ERROR_RESPONSE_EVENT,  ex.getMessage());
            }
         }, c::disconnect);
    }

    private void onCancelJob(SocketIOClient c, HEADJobCancelDto req, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                listenerSocketServerStaff.cancelJobOffer(uuid, req.idJob(), ack);
            } catch (Exception ex) {
                emitter.toUser(uuid, ERROR_RESPONSE_EVENT,  ex.getMessage());
            }
        }, c::disconnect);
    }

    private void onCancelStaff(SocketIOClient c, HEADJobCancelWithNoteDto req, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                listenerSocketServerStaff.onCancelToStaff(uuid, req, ack);
            } catch (Exception ex) {
                emitter.toUser(uuid, ERROR_RESPONSE_EVENT,  ex.getMessage());
            }
        }, c::disconnect);
    }

    private void onSchedulePropose(SocketIOClient c, HEADStaffScheduleProposeMultiRequest req, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                var response = jobService.staffProposeScheduleMulti(uuid, req);
                if (response.success()) {
                    ack.sendAckData(HEADAckResponse.oks());
                }
                else {
                    ack.sendAckData(HEADAckResponse.fail("Error al proponer los horarios al cliente"));
                }
            } catch (Exception ex) {
                emitter.toUser(uuid, ERROR_RESPONSE_EVENT, ex.getMessage());
            }
        }, c::disconnect);
    }

    private void onDecisionOpened(SocketIOClient c, HEADJobIdDto request, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                listenerSocketServerStaff.decisionOpened(uuid, request.jobId(), ack);
            }  catch (Exception ex) {
                log.warn("[DECISION_OPENED] jobId={} staffUuid={}", request.jobId(), uuid);
                log.error("[DECISION_OPENED] decisionOpened jobId={} messageError{} ", request.jobId(), ex.getMessage());
                emitter.toUser(uuid, ERROR_RESPONSE_EVENT, ex.getMessage());
            }
        }, c::disconnect);
    }

    @Transactional
    private void onReconnected(String uuid) {

        var job = jobService.currentForStaff(uuid);
        if (job != null && job.getState() == HEADJobState.READY) {
            callHandler.startForJob(
                    job.getId(),
                    job.getClient().getUuIdUser(),
                    uuid,
                    HEADCallContextType.JOB,
                    job.getRequest().getPkg().getId()
            );
        }
    }

}


