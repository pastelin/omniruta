package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADRequestNotificationActions {
    private String tokenAccess;
    private Boolean isRejectedService;
    private String uuIdClient;
}
