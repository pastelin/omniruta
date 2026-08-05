package com.HealthAdvanced.healthAdvanced.HEADPersonal.listenerPersonal.mappingPersonalSocket;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADWebSocketUsersEntity;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.Request.HEADRequestNotificationActions;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.Response.HEADClientToPersonalResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationMapService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationPersonalService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActivePersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HEADPersonalRequestMap {
    @Autowired
    HEADActiveLocationPersonalService headActiveLocationPersonalService;
    @Autowired
    HEADActiveLocationMapService headActiveLocationMapService;
    ObjectMapper objectMapper = new ObjectMapper();
    public HEADWebSocketUsersEntity parseToRequestPersonal(String jsonPersonal) {
        try {
            return objectMapper.readValue(jsonPersonal, HEADWebSocketUsersEntity.class);
        }catch (JsonProcessingException exception) {
            return null;
        }
    }

    public HEADRequestNotificationActions parseRequestIsRejected(String jsonPersonal) {
        try {
            return objectMapper.readValue(jsonPersonal, HEADRequestNotificationActions.class);
        }catch (JsonProcessingException exception) {
            return null;
        }
    }

    public HEADClientToPersonalResponse mapInfoPersonalToClient(HEADActiveLocationPersonal setLocationPersonal,
                                                                HEADPersonalUser userDetail,
                                                                HEADActivePersonal clientCurrent) {
        var infoPersonal = new HEADClientToPersonalResponse();
        infoPersonal.setNamePersonal(userDetail.getNombre());
        infoPersonal.setLastNamePersonal(userDetail.getAPaterno());
        infoPersonal.setLongitude(setLocationPersonal.getLongitude());
        infoPersonal.setLatitude(setLocationPersonal.getLatitude());
        infoPersonal.setUuIdPersonal(setLocationPersonal.getUuIdPersonal());
        var setDistanceMts = headActiveLocationMapService.calculateDistanceInMetersSafe(clientCurrent.getLatitude(),
                clientCurrent.getLongitude(),
                setLocationPersonal.getLatitude(),setLocationPersonal.getLongitude());
        infoPersonal.setDistanceMts(setDistanceMts);
        return infoPersonal;
    }
}
