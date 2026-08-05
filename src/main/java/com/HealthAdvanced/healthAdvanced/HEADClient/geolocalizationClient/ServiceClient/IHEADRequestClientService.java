package com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.ServiceClient;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.entity.RequestOutcome;
import com.corundumstudio.socketio.SocketIOClient;

import java.util.List;
import java.util.Map;

public interface IHEADRequestClientService {
    Object getPersonalsAvailable(HEADClientLocationPackage requestJson);
    RequestOutcome requestServiceClient(String userUuid, HEADClientLocationPackage requestJson);
    Map<String, Object> updateLocationClientCurrent(String userUuid, String jsonClientLocation);
}