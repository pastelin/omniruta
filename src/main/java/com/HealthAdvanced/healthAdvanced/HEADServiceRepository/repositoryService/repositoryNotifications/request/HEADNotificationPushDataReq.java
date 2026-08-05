package com.HealthAdvanced.healthAdvanced.HEADServiceRepository.repositoryService.repositoryNotifications.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADNotificationPushDataReq {
    private String title;
    private String body;
}
