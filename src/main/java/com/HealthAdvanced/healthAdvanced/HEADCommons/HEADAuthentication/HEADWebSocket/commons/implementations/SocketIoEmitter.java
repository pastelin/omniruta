package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.implementations;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADClientUpdateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADOfferDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.events.HEADJobEvents;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADEventsClientConst.CLIENT_ROOM_PREFIX;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADEventsClientConst.NS_PATH_CLIENT;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADEventsStaffConst.*;

@Component
public class SocketIoEmitter implements HEADWsEmitter {
    private final SocketIOServer server;

    public SocketIoEmitter(SocketIOServer server) { this.server = server; }
    private static String staffRoomFor(String uuid) { return STAFF_ROOM_PREFIX + uuid; }
    private static String clientRoomFor(String uuid) {return CLIENT_ROOM_PREFIX + uuid; }

    private SocketIONamespace staffNs()  { return server.getNamespace(NS_PATH);  }
    private SocketIONamespace clientNs() { return server.getNamespace(NS_PATH_CLIENT); }

    @Override
    public void toUser(String userUuid, String event, Object payload) {
        staffNs().getRoomOperations(staffRoomFor(userUuid)).sendEvent(event, HEADWsEnvelope.ok(event,payload));
    }

    @Override
    public void toSession(String sessionId, String event, Object payload) {
        var client = server.getClient(UUID.fromString(sessionId));
        if (client != null) client.sendEvent(event, payload);
    }


    @Override
    /** Útil en tus listeners onConnect/onReauth si quieres sumarlos al room del job. */
    public void addClientToJob(SocketIOClient c, Long jobId) {
        c.joinRoom(HEADJobEvents.roomOf(jobId));
    }

    @Override
    public void emitOffer(String staffUuid, HEADOfferDto payload) {
        toUser(staffUuid, EVENT_JOB_OFFER, payload);
    }

    @Override
    public void emitToClient(String userUuid, String event, Object payload) {
        clientNs().getRoomOperations(clientRoomFor(userUuid)).sendEvent(event, HEADWsEnvelope.ok(event,payload));
    }

    @Override
    public void emitJobAcceptedToStaff(String uuIdUser, Long jobId) {
        toUser(uuIdUser, JOB_ACCEPT,jobId);
    }
}

