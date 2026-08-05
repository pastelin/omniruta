package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADAckResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.request.*;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket.HEADCallSocketEvents.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADCallSignalingHandler {

    private final HEADPresenceStore presence;
    private final HEADCallWsEmitter callEmitter;
    private final HEADCallRoutingStore routing;

    private static String uuidOf(SocketIOClient c) { return c.get("userUuid"); }

    private void ackOk(AckRequest ack) {
        if (ack != null && ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
    }
    private void ackFail(AckRequest ack, String code) {
        if (ack != null && ack.isAckRequested()) ack.sendAckData(HEADAckResponse.fail(code));
    }

    private void emitTo(HEADCallRoutingStore.Participant p, String event, Object payload) {
        callEmitter.toUser(p.uuid(), p.type(), event, payload);
    }

    private void ensureParticipant(String callId, String me) {
        if (!routing.isParticipant(callId, me)) throw new HEADBadRequestException("NOT_PARTICIPANT");
    }

    /** ✅ Se llama cuando staff acepta la oferta / job queda asignado */
    public String startForJob(
            Long jobId,
            String clientUuid,
            String staffUuid,
            HEADCallContextType contextType,
            String contextId
    ) {
        var callId = java.util.UUID.randomUUID().toString();

        var client = new HEADCallRoutingStore.Participant(clientUuid, HEADChatParticipantType.CLIENT);
        var staff  = new HEADCallRoutingStore.Participant(staffUuid, HEADChatParticipantType.STAFF);

        routing.bind(callId, client, staff);

        // staff = offerer
        emitTo(staff, HEADCallSocketEvents.CALL_START,
                new HEADCallStartDto(callId, jobId, true,  contextType, contextId));

        emitTo(client, HEADCallSocketEvents.CALL_START,
                new HEADCallStartDto(callId, jobId, false, contextType, contextId));

        // si mantienes state update, aquí sería STARTED/CONNECTING
        // broadcastState(callId, HEADCallState.CONNECTING, null);

        return callId;
    }

    public void onSdp(SocketIOClient c, HEADCallSdpDto dto, AckRequest ack) {
        var me = uuidOf(c);
        if (me == null) { c.disconnect(); return; }
        presence.renew(c.getSessionId().toString());
        ensureParticipant(dto.callId(), me);
        routing.touch(dto.callId());

        routing.other(dto.callId(), me).ifPresent(other -> emitTo(other, HEADCallSocketEvents.CALL_SDP, dto));
        ackOk(ack);
    }

    public void onIce(SocketIOClient c, HEADCallIceCandidateDto dto, AckRequest ack) {
        var me = uuidOf(c);
        if (me == null) { c.disconnect(); return; }

        presence.renew(c.getSessionId().toString());
        ensureParticipant(dto.callId(), me);
        routing.touch(dto.callId());

        routing.other(dto.callId(), me).ifPresent(other -> emitTo(other, HEADCallSocketEvents.CALL_ICE_CANDIDATE, dto));
        ackOk(ack);
    }

    public void onEnd(SocketIOClient c, HEADCallEndRequest req, AckRequest ack) {
        var me = uuidOf(c);
        if (me == null) { c.disconnect(); return; }

        presence.renew(c.getSessionId().toString());
        ensureParticipant(req.callId(), me);

        // emite al peer antes de limpiar (para que el otro cierre UI)
        routing.other(req.callId(), me).ifPresent(other ->
                emitTo(other, HEADCallSocketEvents.CALL_END, req)
        );

        routing.remove(req.callId());
        ackOk(ack);
    }

    /** ✅ Útil para kill app / disconnect */
    public void forceEnd(String callId, HEADCallEndReason reason) {
        routing.getA(callId).ifPresent(p -> emitTo(p, HEADCallSocketEvents.CALL_END,
                new HEADCallEndRequest(callId, reason)));
        routing.getB(callId).ifPresent(p -> emitTo(p, HEADCallSocketEvents.CALL_END,
                new HEADCallEndRequest(callId, reason)));

        routing.remove(callId);
    }

    /** ✅ Llamar desde tu onDisconnect(SocketIOClient c) */
    public void onSocketDisconnected(SocketIOClient c) {
        var me = uuidOf(c);
        if (me == null) return;

        routing.activeCallOf(me).ifPresent(callId -> forceEnd(callId, HEADCallEndReason.PEER_DISCONNECTED));
    }
}
