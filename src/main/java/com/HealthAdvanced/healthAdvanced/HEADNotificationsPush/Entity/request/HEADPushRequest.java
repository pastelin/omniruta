package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.request;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;

import java.util.Map;

public record HEADPushRequest(
        String userUuid,
        HEADNotificationType type,
        String title,
        String body,
        Map<String, String> data
) {}

