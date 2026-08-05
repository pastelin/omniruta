package com.HealthAdvanced.healthAdvanced.HEADServiceRepository.Service;

import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.Dtos.request.HEADRequestServiceClient;
import com.corundumstudio.socketio.SocketIOClient;

public interface IHEADServiceResultApi<T> {
    void onSuccess(T objectResponse,Integer idUserClient, HEADRequestServiceClient headRequestServiceClient, SocketIOClient client);

    void onError(String errorMessage);
}
