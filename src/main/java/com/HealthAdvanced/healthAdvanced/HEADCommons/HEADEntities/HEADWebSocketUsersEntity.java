package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADWebSocketUsersEntity {
    private String tokenAccess;
    private Double latitude;
    private Double longitude;
    private Boolean isActiveWork;
    private Boolean isBusy;
    private String uuIdPersonal;
    private String idSocketUser;
    private long distanceMts;
}
