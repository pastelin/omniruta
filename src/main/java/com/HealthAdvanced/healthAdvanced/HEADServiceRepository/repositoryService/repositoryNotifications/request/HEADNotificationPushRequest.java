package com.HealthAdvanced.healthAdvanced.HEADServiceRepository.repositoryService.repositoryNotifications.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADNotificationPushRequest {
    private String to;
    private String priority;
    HEADNotificationPushDataReq data;
}
