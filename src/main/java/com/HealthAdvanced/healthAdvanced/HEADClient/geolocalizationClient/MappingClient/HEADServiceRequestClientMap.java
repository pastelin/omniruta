package com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.MappingClient;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADClientCommons.HEADRequestClientConstants;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.Dtos.request.HEADRequestServiceClient;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADWebSocketUsersEntity;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationPersonalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class HEADServiceRequestClientMap {
    @Autowired
    private HEADActiveLocationPersonalService headActiveLocationPersonalService;
    ObjectMapper objectMapper = new ObjectMapper();
    public String headMapPersonalsAvailable(HEADClientLocationPackage dataObject) throws JsonProcessingException {
        var getProfiles = headActiveLocationPersonalService.nearbyPersonalActive(dataObject.getUserLat(),
                dataObject.getUserLong());
        return objectMapper.writeValueAsString(HEADApiResponse.ok(getProfiles));
    }

    public HEADRequestServiceClient parseToRequestClient(String jsonClient) {
        try {
            return objectMapper.readValue(jsonClient, HEADRequestServiceClient.class);
        }catch (JsonProcessingException exception) {
            return null;
        }
    }

    public HEADClientLocationPackage locationClientRequest(String jsonClient) {
        try {
            return objectMapper.readValue(jsonClient, HEADClientLocationPackage.class);
        }catch (JsonProcessingException exception) {
            return null;
        }
    }
}
