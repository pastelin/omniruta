package com.HealthAdvanced.healthAdvanced.HEADPersonal.listenerPersonal.personalRequest;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Repository.HEADGeolocationRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationMapService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationPersonalService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActivePersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADActivePersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.listenerPersonal.mappingPersonalSocket.HEADPersonalRequestMap;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.SEND_NOTIFICATION_TO_CLIENT;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils.generatorUUID;

@Service
public class HEADPersonalRequestService {

    @Autowired
    HEADActiveLocationPersonalService headActiveLocationPersonalService;
    @Autowired
    HEADActiveLocationMapService headActiveLocationMapService;
    @Autowired
    HEADGeolocationRepository headGeolocationRepository;
    @Autowired
    HEADPersonalUserRepository personaUserRepository;

    @Autowired
    SocketIOServer server;

    @Autowired
    HEADActivePersonalRepository headActivePersonalRepository;
    @Autowired
    HEADPersonalRequestMap headPersonalRequestMap;

    @Autowired
    HEADJwtGenerator headJwtGenerator;


    public void updateActiveLocation(List<SocketIOClient> clients, SocketIOClient client, String jsonPersonalLocation) {
        var dto = headPersonalRequestMap.parseToRequestPersonal(jsonPersonalLocation);
        var uuidPersonal = headJwtGenerator.extractUsername(dto.getTokenAccess());
        var userDetail = personaUserRepository.findByUidUser(uuidPersonal).orElse(new HEADPersonalUser());

        HEADActiveLocationPersonal current = headGeolocationRepository.findByIdPersonalUser(
                headActiveLocationMapService.createActiveLocationPersonal(dto, userDetail).getIdPersonalUser()
        ).orElse(null);

        String room = "user:" + uuidPersonal;

        if (current != null) {
            // BEFORE: sessionId
            // current.setIdSocketPersonal(client.getSessionId().toString());
            // NOW: room estable
            current.setIdSocketPersonal(room);

            current.setDateCurrent(HEADCommonsUtils.getDateTimeCurrent());
            current.setLatitude(dto.getLatitude());
            current.setLongitude(dto.getLongitude());
            current.setIsActiveWork(dto.getIsActiveWork());
            current.setIsBusy(dto.getIsBusy());
            headGeolocationRepository.save(current);
            headActiveLocationPersonalService.sendRefreshLocationToClient(current);
        } else {
            dto.setUuIdPersonal(generatorUUID());
            // BEFORE:
            // domain.setIdSocketUser(client.getSessionId().toString());
            // NOW:
            dto.setIdSocketUser(room);

            var saved = headActiveLocationPersonalService.saveActiveLocationPersonal(dto, userDetail);
            headActiveLocationPersonalService.sendRefreshLocationToClient(saved);
        }
    }


    public void isAcceptedToClient(List<SocketIOClient> clients, String data) {
        var headPersonalEntity = headPersonalRequestMap.parseRequestIsRejected(data);
        var setPersonalUUID = headJwtGenerator.extractUsername(headPersonalEntity.getTokenAccess());
        var userDetail = personaUserRepository.findByUidUser(setPersonalUUID).orElse(new HEADPersonalUser());
        var getClientActive = headActivePersonalRepository.findByUuIdClient(headPersonalEntity.getUuIdClient()).orElse(new ArrayList<>());
        var setClientCurrent = getClientActive.stream().filter(getClient -> getClient.getIsRejected() == null).findFirst().orElse(new HEADActivePersonal());
        setClientCurrent.setIsRejected(headPersonalEntity.getIsRejectedService());
        setClientCurrent.setIdPersonalUser(userDetail);
        headActivePersonalRepository.save(setClientCurrent);
        String stored = setClientCurrent.getIdSocketClient();
        var setLocationPersonal = headGeolocationRepository.findByIdPersonalUser(userDetail).orElse(new HEADActiveLocationPersonal());
        var infoPersonal = headPersonalRequestMap.mapInfoPersonalToClient(setLocationPersonal,userDetail,setClientCurrent);// ahora debería ser "user:uuid"
        if (stored != null && stored.startsWith("user:")) {
            server.getRoomOperations(stored).sendEvent(SEND_NOTIFICATION_TO_CLIENT, infoPersonal);
        } else {
            clients.stream()
                    .filter(c -> c.getSessionId().toString().equals(stored))
                    .findFirst()
                    .ifPresent(c -> c.sendEvent(SEND_NOTIFICATION_TO_CLIENT, infoPersonal));
        }

    }
}
