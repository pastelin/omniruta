package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesMaps.HEADPackagesMaps;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.maps.HEADShowStaffsToClientsMap;
import com.HealthAdvanced.healthAdvanced.HEADClient.listenerClientWebSocket.HEADListenerSocketServerClient;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADAckResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.implementations.HEADListenersSocket;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADUserDirectory;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADListenerSocketServerStaff;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADChatPresenceUpdateSocketDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADJobCancelDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADLocUpdateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.HEADChatSendMessageRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse.HEADChatPresenceDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventsListeners.HEADEventsListenersSocket;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallContextType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallEndReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.request.HEADCallAcceptRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.request.HEADCallCreateRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.request.HEADCallEndRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.request.HEADCallRejectRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response.HEADCallIceCandidateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response.HEADCallSdpDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket.HEADCallRoutingStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket.HEADCallSignalingHandler;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobQueryService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADRideAssignmentService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request.HEADClientScheduleSelectRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.dtos.HEADNearbyWatchReq;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADNearbyService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.api.HEADListenerSocketServerClientRating;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.request.HEADSubmitReviewRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request.HEADPrescriptionDraftJoinRequest;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.socket.HEADListenerSocketPrescription;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.request.HEADScheduleRequest;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.enums.HEADRole.*;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.*;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADEventsClientConst.*;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADEventsStaffConst.ACK_OK;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADEventsStaffConst.JOB_CANCEL;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventsName.HEADChatWsEvents.*;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket.HEADCallSocketEvents.*;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket.HEADCallSocketEvents.CALL_END;
import static com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.api.HEADRatingWsEvents.CLIENT_REVIEW_SUBMIT;
import static com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.api.HEADRatingWsEvents.REVIEW_OK;
import static com.HealthAdvanced.healthAdvanced.HEADPrescription.socket.HEADPrescriptionEvents.PRESCRIPTION_DRAFT_JOIN;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADClientWebSocketListener {
    private final SocketIOServer server;
    private SocketIONamespace ns; // /staff
    private final HEADJwtGenerator jwt;
    private final HEADPresenceStore presence;
    private final HEADClientStateStore clientState;
    private final HEADJobQueryService jobQuery;
    private final HEADUserDirectory userDirectory;
    private final HEADWsEmitter emitter;
    private final HEADListenerSocketServerClient clientAdapter;
    private final HEADNearbyService nearbyService;
    private final HEADPackagesMaps packageDirectory;
    private final HEADShowStaffsToClientsMap headShowStaffsToClientsMap;
    private final HEADJobService headJobService;
    private final HEADEventsListenersSocket headEventsListenersSocket;
    private final HEADCallSignalingHandler callHandler;
    private final HEADCallRoutingStore callRouting;
    private final HEADListenerSocketServerClientRating ratingListener;
    private final HEADListenerSocketPrescription listenerSocketPrescription;
    private final HEADRideAssignmentService rideAssignmentService;
    private final HEADListenerSocketServerStaff listenerSocketServerStaff;

    private final AtomicBoolean registered = new AtomicBoolean(false);

    @PostConstruct
    void init() {
        this.ns = server.addNamespace(NS_PATH_CLIENT);

        var listeners = new HEADListenersSocket(ns, new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        );

        ns.addConnectListener(this::onConnect);
        ns.addDisconnectListener(this::onDisconnect);

        // heartbeats / reauth
        ns.addEventListener(CLIENT_HEARTBEAT, String.class, this::onHeartbeat);
            ns.addEventListener(CLIENT_REAUTH, String.class, this::onReauth);

        // estado / ubicación
        //ns.addEventListener(CLIENT_ONLINE_TOGGLE, HEADStaffOnlineToggleDto.class, this::onOnlineToggle);
        listeners.onTx(CLIENT_LOC_UPDATE, HEADLocUpdateDto.class, this::onLocUpdate);

        listeners.onTx(REQUEST_SERVICE_CLIENT, HEADClientLocationPackage.class, this::requestServiceClient);
        listeners.onTx(CLIENT_OFFER_SCHEDULE_SELECT, HEADClientScheduleSelectRequest.class, this::scheduleSelect);

        listeners.onTx(HEADWsEvents.NEARBY_SUBSCRIBE, HEADNearbyWatchReq.class, (c, req, ack) -> {
            Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
                presence.renew(c.getSessionId().toString());
                var snap = nearbyService.start(uuid, req);
                if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
            }, c::disconnect);
        });

        listeners.onTx(HEADWsEvents.NEARBY_UNSUBSCRIBE, String.class, (c, _unused, ack) -> {
            presence.renew(c.getSessionId().toString());
            nearbyService.stop(c.getSessionId().toString());
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
        });

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
        registerCallListeners(listeners, HEADChatParticipantType.CLIENT);
        listeners.onTx(CLIENT_REVIEW_SUBMIT, HEADSubmitReviewRequest.class, ratingListener::onSubmitReview);
        listeners.onTx(PRESCRIPTION_DRAFT_JOIN, HEADPrescriptionDraftJoinRequest.class, listenerSocketPrescription::onDraftJoin);
        listeners.onTx(CLIENT_REQUEST_SCHEDULE_PROPOSAL, HEADScheduleRequest.class, this::getScheduleCurrent);
        listeners.onTx(JOB_CANCEL, HEADJobCancelDto.class, this::onCancelJob);
        listeners.onTx(JOB_CANCEL_CLIENT, HEADJobCancelDto.class, this::onCancelToStaff);
    }

    // ---------- helpers ----------
    public static String clientRoom(String uuid) { return CLIENT_ROOM_PREFIX.concat(uuid); }

    @SuppressWarnings("unchecked")
    private static List<String> rolesFrom(Map<String, Object> claims) {
        Object r = claims.get("roles");
        return (r instanceof List) ? (List<String>) r : List.of();
    }

    private static String uuidOf(SocketIOClient c) { return c.get("userUuid"); }

    private void joinActiveJobRooms(SocketIOClient c, String clietnUuid) {
        Optional.ofNullable(clietnUuid)
                .map(userDirectory::findClientUserIdByUuid)
                .ifPresent(clientUserId -> jobQuery.findActiveJobIdsForClientUserId(clientUserId)
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
                        c.joinRoom(clientRoom(uuid));
                        presence.add(c.getSessionId().toString(), uuid);
                        clientState.setAppActive(uuid, true);
                        Optional.ofNullable(clientState.get(uuid))
                                .ifPresent(state -> c.sendEvent(CLIENT_STATE, state));
                        joinActiveJobRooms(c, uuid);
                        onReconnected(uuid);
                    } catch (Exception e) {
                        c.disconnect();
                    }
                }, c::disconnect);
    }


    private void onDisconnect(SocketIOClient c) {
        presence.remove(c.getSessionId().toString());
        String uuid = uuidOf(c);
        if (uuid != null) {
            boolean stillOnline = presence.isOnline(uuid);
            if (!stillOnline) clientState.setAppActive(uuid, false);
            callRouting.activeCallOf(uuid).ifPresent(callId -> {
                callHandler.forceEnd(callId, HEADCallEndReason.NETWORK);
            });
        }
    }

    // ---------- auth/heartbeat ----------
    private void onHeartbeat(SocketIOClient c, String _payload, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            presence.renew(c.getSessionId().toString());
            clientState.heartbeat(uuid);
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
                    .ifPresent(old -> c.leaveRoom(clientRoom(old)));

            c.set("userUuid", newUuid);
            c.joinRoom(clientRoom(newUuid));
            presence.update(c.getSessionId().toString(), newUuid);

            Optional.ofNullable(clientState.get(newUuid))
                    .ifPresent(state -> c.sendEvent(CLIENT_STATE, state));
            joinActiveJobRooms(c, newUuid);
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.fail("TOKEN_EXPIRED"));
        } catch (Exception e) {
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.fail(e.getMessage()));
        }
    }

    private void onLocUpdate(SocketIOClient c, HEADLocUpdateDto dto, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            presence.renew(c.getSessionId().toString());
            clientState.updateLocation(uuid, dto.lat(), dto.lng());
            //headJobService.currentForClient(uuid);
            log.info("onLocUpdate domain={}", dto);
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
        }, c::disconnect);
    }

    private void requestServiceClient(SocketIOClient c, HEADClientLocationPackage object, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                clientAdapter.onRequestServiceClient(c, object, ack);
                log.info("requestServiceClient domain={}", object);
            } catch (HEADBadRequestException ex) {
                if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.fail(ex.getMessage()));
            } catch (Exception ex) {
                if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.fail("ERROR"));
            }
        }, c::disconnect);
    }


    private void registerCallListeners(HEADListenersSocket listeners, HEADChatParticipantType fromType) {

        listeners.onTx(CALL_SDP, HEADCallSdpDto.class, callHandler::onSdp);
        listeners.onTx(CALL_ICE_CANDIDATE, HEADCallIceCandidateDto.class, callHandler::onIce);

        listeners.onTx(CALL_END, HEADCallEndRequest.class, callHandler::onEnd);
    }

    private void scheduleSelect(SocketIOClient c, HEADClientScheduleSelectRequest request, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                var responseAck = headJobService.offerScheduleSelect(uuid, request);
                if (ack.isAckRequested() && responseAck != null) {
                    rideAssignmentService.cancelSchedulePendingTimeout(request.jobId());
                    ack.sendAckData(HEADWsEnvelope.ok(REVIEW_OK, responseAck));
                } else {
                    ack.sendAckData(HEADWsEnvelope.fail("REVIEW_FAIL"));
                }
            } catch (Exception ex) {
                log.info("[SCHEDULE] message={}", ex.getMessage());
                ack.sendAckData(HEADAckResponse.fail("Error al cancelar: " + ex.getMessage()));
            }
        }, c::disconnect);
    }

    private void getScheduleCurrent(SocketIOClient c, HEADScheduleRequest req, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            presence.renew(c.getSessionId().toString());
            var responseAck = headJobService.getScheduleCurrent(req.jobId());
            if (responseAck != null) {
                ack.sendAckData(HEADWsEnvelope.ok("Se obtuvo los horarios correctamente", responseAck));
            }
            else {
                emitter.emitToClient(uuid, ERROR_RESPONSE_EVENT,"Error al obtener los horarios");
                ack.sendAckData(HEADWsEnvelope.fail("Error al obtener los horarios"));
            }
        }, c::disconnect);
    }

    private void onCancelJob(SocketIOClient c, HEADJobCancelDto req, AckRequest ack) {
        Optional.ofNullable(uuidOf(c)).ifPresentOrElse(uuid -> {
            try {
                presence.renew(c.getSessionId().toString());
                var jobCurrent = headJobService.findById(req.idJob());
                listenerSocketServerStaff.cancelJobOffer(jobCurrent.getStaffUuid(), req.idJob(), ack);
            } catch (Exception ex) {
                emitter.emitToClient(uuid, ERROR_RESPONSE_EVENT, ex.getMessage());
            }
        }, c::disconnect);
    }

    private void onCancelToStaff(SocketIOClient c, HEADJobCancelDto req, AckRequest ack) {
        var uuid = uuidOf(c);
        if (uuid == null) {
            ack.sendAckData(HEADAckResponse.fail("Sesión inválida"));
            c.disconnect();
            return;
        }

        try {
            presence.renew(c.getSessionId().toString());
            headJobService.cancelByClient(uuid,req.idJob(),HEADCancelReason.CLIENT_CANCELLED, "No encontro Staff");
            ack.sendAckData(HEADAckResponse.oks());

        } catch (HEADBadRequestException ex) {
            emitter.emitToClient(uuid, ERROR_RESPONSE_EVENT, ex.getMessage());
            ack.sendAckData(HEADAckResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            emitter.emitToClient(uuid, ERROR_RESPONSE_EVENT, ex.getMessage());
            ack.sendAckData(HEADAckResponse.fail("Error al cancelar, inténtalo más tarde: " + ex.getMessage()));
        }
    }


    @Transactional
    private void onReconnected(String uuid) {
        var job = headJobService.currentForClient(uuid);
        log.info("[RECONNECTED CLIENT] jobId={}",job != null ? job.getId() : null);
        if (job != null && job.getState() == HEADJobState.READY) {
            callHandler.startForJob(
                    job.getId(),
                    uuid,
                    job.getStaffUuid(),
                    HEADCallContextType.JOB,
                    job.getRequest().getPkg().getId()
            );
        }
    }
}
