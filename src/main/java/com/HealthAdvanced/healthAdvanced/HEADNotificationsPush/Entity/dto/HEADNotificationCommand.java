package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;

import java.util.Locale;
import java.util.Map;

public record HEADNotificationCommand(
        String userUuid,
        HEADNotificationType type,
        String templateCode,
        Map<String, Object> params,
        Locale locale
) {
    public HEADNotificationCommand(String userUuid,
                                   HEADNotificationType type,
                                   String templateCode,
                                   Map<String, Object> params) {
        this(userUuid, type, templateCode, params, Locale.forLanguageTag("es-MX"));
    }
}



