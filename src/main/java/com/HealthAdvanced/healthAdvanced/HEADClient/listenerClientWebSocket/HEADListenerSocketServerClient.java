package com.HealthAdvanced.healthAdvanced.HEADClient.listenerClientWebSocket;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.service.HEADShowStaffsToClientsService;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.ServiceClient.HEADRequestClientService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADAckResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADClientUpdateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.events.HEADJobEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.ASSIGNMENT_FAILED;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.ASSIGNMENT_STARTED;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADEventsStaffConst.EVENT_CLIENT_UPDATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADListenerSocketServerClient {

    private final HEADRequestClientService service;
    private final HEADWsEmitter emitter;
    private final HEADShowStaffsToClientsService headShowStaffsToClientsService;

    public void onGetPersonalsAvailable(SocketIOClient client, HEADClientLocationPackage raw, AckRequest ack) {
        var result = service.getPersonalsAvailable(raw); // devuelve DTO
        if (ack.isAckRequested()) ack.sendAckData(result); // o responde por evento
        else client.sendEvent(HEADWsEvents.PERSONAL_AVAILABLE_RESPONSE, result);
    }

    public void onRequestServiceClient(SocketIOClient client, HEADClientLocationPackage raw, AckRequest ack) {
        try {
            String userUuid = client.get("userUuid");

            HEADJob job = headShowStaffsToClientsService.startNewJobAssignment(raw, userUuid);

            if (job == null) {
                if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
                return;
            }

            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.oks());
            client.joinRoom(HEADJobEvents.roomOf(job.getId()));

        } catch (HEADBadRequestException ex) {
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.fail( ex.getMessage()));
        } catch (Exception ex) {
            if (ack.isAckRequested()) ack.sendAckData(HEADAckResponse.fail("ERROR"));
        }
    }



    public void onUpdateLocationClientCurrent(SocketIOClient client, String raw, AckRequest ack) {
        String userUuid = client.get("userUuid");
        var ok = service.updateLocationClientCurrent(userUuid, raw);
        log.info("onLocUpdate domain={}", userUuid);
        if (ack.isAckRequested()) ack.sendAckData(ok);
        // notifica a toda la room del usuario
        emitter.toUser(userUuid, HEADWsEvents.CLIENT_LOCATION_SAVED, ok);
    }

}
