package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model;

public record HEADFcmAndroidOptions(
        String priority,     // HIGH | NORMAL
        Integer ttlSeconds,
        String collapseKey,
        String channelId,
        String tag
) {
    public static HEADFcmAndroidOptions defaults() {
        return new HEADFcmAndroidOptions("HIGH", 3600, null, "head_push", null);
    }
}
