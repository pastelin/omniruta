package com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.Dtos.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADRequestServiceClientInfo {
    private String tokenAccess;
    private Double latitude;
    private Double longitude;
    private String tokenNotification;
}
