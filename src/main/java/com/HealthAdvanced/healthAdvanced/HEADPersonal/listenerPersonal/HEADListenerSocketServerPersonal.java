package com.HealthAdvanced.healthAdvanced.HEADPersonal.listenerPersonal;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.listenerPersonal.personalRequest.HEADPersonalRequestService;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HEADListenerSocketServerPersonal {
    @Autowired
    HEADPersonalRequestService headPersonalRequestService;
    public void updateActiveLocation(List<SocketIOClient> clients, SocketIOClient client, String data) {
        headPersonalRequestService.updateActiveLocation(clients,client,data);
    }

    public void sendNotificationToClient(List<SocketIOClient> clients, String data) {
        headPersonalRequestService.isAcceptedToClient(clients,data);
    }
}
