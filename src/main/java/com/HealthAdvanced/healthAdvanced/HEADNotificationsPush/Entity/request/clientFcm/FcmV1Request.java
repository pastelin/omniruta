package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.request.clientFcm;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FcmV1Request(Message message) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Message(
            String token,
            Notification notification,
            Map<String, String> data,
            Android android
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Notification(
            String title,
            String body
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Android(
            String priority,                 // "HIGH" | "NORMAL"
            String ttl,                      // e.g. "3600s"
            @JsonProperty("collapse_key")
            String collapseKey,
            AndroidNotification notification
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AndroidNotification(
            @JsonProperty("channel_id")
            String channelId,
            String tag,                      // Android replace/dedupe
            String sound                     // "default"
    ) {}
}
