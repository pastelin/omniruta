package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.Response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADClientToPersonalResponse {
    private Double longitude;
    private Double latitude;
    private String namePersonal;
    private String lastNamePersonal;
    private long distanceMts;
    private String uuIdPersonal;
}
