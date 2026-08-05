package com.HealthAdvanced.healthAdvanced.HEADPersonal.pushNotificationsPersonal.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADRequestServiceNotificationPush {
    private String title;
    private String body;
    private String uuIdClient;
    private long distance;
}
