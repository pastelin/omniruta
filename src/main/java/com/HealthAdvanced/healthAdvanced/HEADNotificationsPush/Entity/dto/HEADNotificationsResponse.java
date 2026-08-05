package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;

public record HEADNotificationsResponse(
        Summary summary,
        HEADPageResponse<Item> page
) {
    public record Summary(long total, long unread) {}

    public record Item(
            long id,
            String type,     // "APPOINTMENT" | "REMINDER" | "PROMOTION" | "INFO"
            String title,
            String message,
            String time,     // "Hace 5 min" (o formato fecha)
            boolean isRead,
            String icon
    ) {}
}