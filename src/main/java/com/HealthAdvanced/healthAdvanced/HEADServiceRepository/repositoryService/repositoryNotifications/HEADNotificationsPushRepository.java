package com.HealthAdvanced.healthAdvanced.HEADServiceRepository.repositoryService.repositoryNotifications;

import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.Dtos.request.HEADRequestServiceClient;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.Service.IHEADServiceGeneric;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.Service.IHEADServiceResultApi;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.repositoryService.repositoryNotifications.request.HEADNotificationPushRequest;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.repositoryService.repositoryNotifications.response.HEADNotificationsPushResponse;
import com.corundumstudio.socketio.SocketIOClient;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HEADNotificationsPushRepository implements IHEADNotificationsPushRepository{
    @Autowired
    @Qualifier("HEADServiceGeneric")
    private IHEADServiceGeneric iheadServiceGeneric;

    private String endPointSendNotificationPush = "";

    private String keyNotificationApi = "key=AAAAwNrbqwc:APA91bGuHq_xixi5ODPBPHLfQLYzE6OZCw3xN2r5MpErnG2NRY0Gli11cy3zIXVsNVptNYJYasUabQQZpWh9nND7esQ0eMhnzvpgvto_E5kEiVIjQ58RIGv38fPuFIi6J26Ry9kX-9nF";

    /*@Override
    public void sendNotificationPush(HEADNotificationPushRequest headNotificationPushRequest,
                                     IHEADServiceResultApi serviceCallBack,
                                     SocketIOClient client, Integer idUserClient,
                                     HEADRequestServiceClient headRequestServiceClient) {
        endPointSendNotificationPush = "/fcm/send";
        headNotificationPushRequest.setPriority("high");
        WebClient webClient = iheadServiceGeneric.webClientBuilder();
        webClient.post()
                .uri(endPointSendNotificationPush)
                .header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION,keyNotificationApi)
                .bodyValue(headNotificationPushRequest)
                .retrieve()
                .bodyToMono(HEADNotificationsPushResponse.class)
                .subscribe(
                        responseBody -> serviceCallBack.onSuccess(responseBody,idUserClient,headRequestServiceClient,client),
                        error -> serviceCallBack.onError(error.getMessage())
                );
    }*/

}
