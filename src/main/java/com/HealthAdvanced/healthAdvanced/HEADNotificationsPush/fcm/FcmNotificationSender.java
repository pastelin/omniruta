package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.fcm;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.exceptions.HEADFcmSendException;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationText;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model.HEADFcmAndroidOptions;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Service.HEADNotificationInboxService;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Service.HEADNotificationsApiService;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Service.HEADPushTokenService;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces.HEADNotificationSender;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces.HEADNotificationTextComposer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class FcmNotificationSender implements HEADNotificationSender {

    private static final int MAX_DATA_VALUE_LEN = 900;

    private final HEADNotificationTextComposer textComposer;
    private final HEADPushTokenService tokenService;
    private final HEADFcmClient fcmClient;
    private final HEADNotificationInboxService inboxService;

    public FcmNotificationSender(HEADNotificationTextComposer textComposer,
                                 HEADPushTokenService tokenService,
                                 HEADFcmClient fcmClient, HEADNotificationInboxService inboxService) {
        this.textComposer = textComposer;
        this.tokenService = tokenService;
        this.fcmClient = fcmClient;
        this.inboxService = inboxService;
    }

    @Override
    public void send(HEADNotificationCommand command) {


        HEADNotificationText text = null;
        try {
            text = textComposer.compose(command);
        } catch (Exception e) {
            log.error("[FCM] compose FAIL userUuid={} type={} err={}", command.userUuid(), command.type(), e.toString());
            // fallback mínimo
            text = new HEADNotificationText("Notificación", "");
        }
        inboxService.upsertRelevant(command, text);

        List<String> tokens = tokenService.findTokensForUser(command.userUuid());
        if (tokens == null || tokens.isEmpty()) {
            log.info("[FCM] no tokens userUuid={} type={}", command.userUuid(), command.type());
            return;
        }

        Map<String, Object> params = command.params() == null ? Map.of() : command.params();

        Map<String, String> data = new HashMap<>();
        data.put("type", command.type().name());
        params.forEach((k, v) -> data.put(k, safeDataValue(v)));

        HEADFcmAndroidOptions opt = extractAndroidOptions(params);

        HEADNotificationText finalText = text;
        Flux.fromIterable(tokens)
                .flatMap(token ->
                                fcmClient.sendToToken(
                                                token,
                                                safeTitle(finalText.title() != null ? finalText.title() : ""),
                                                safeBody(finalText.body() != null ? finalText.body() : ""),
                                                data,
                                                opt
                                        )
                                        .doOnSuccess(v -> log.info("[FCM] sent ok userUuid={} type={} token={}",
                                                command.userUuid(), command.type(), shortToken(token)))
                                        .doOnError(err -> {
                                            log.info("[FCM] sent FAIL userUuid={} type={} token={} err={}",
                                                    command.userUuid(), command.type(), shortToken(token), err.toString());
                                            if (isInvalidTokenError(err)) tokenService.deactivateToken(token);
                                        })
                                        .onErrorResume(e -> Mono.empty()),
                        8
                )
                .then()
                .subscribe();
    }

    private HEADFcmAndroidOptions extractAndroidOptions(Map<String, Object> p) {
        String priority = Objects.toString(p.getOrDefault("androidPriority", "HIGH"), "HIGH");
        Integer ttl = tryInt(p.get("ttlSeconds"), 3600);
        String collapseKey = (String) p.getOrDefault("collapseKey", null);
        String channelId = Objects.toString(p.getOrDefault("channelId", "head_push"), "head_push");
        String tag = (String) p.getOrDefault("tag", null);
        return new HEADFcmAndroidOptions(priority, ttl, collapseKey, channelId, tag);
    }

    private Integer tryInt(Object v, Integer def) {
        if (v == null) return def;
        try { return Integer.parseInt(String.valueOf(v)); }
        catch (Exception ignore) { return def; }
    }

    private String safeDataValue(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        if (s.length() <= MAX_DATA_VALUE_LEN) return s;
        return s.substring(0, MAX_DATA_VALUE_LEN);
    }

    private String safeTitle(String t) {
        return (t == null || t.isBlank()) ? "Notificación" : t;
    }

    private String safeBody(String b) {
        if (b == null) return "";
        return b.length() > 2000 ? b.substring(0, 2000) : b;
    }

    private String shortToken(String t) {
        if (t == null) return "null";
        return t.length() <= 12 ? t : (t.substring(0, 6) + "..." + t.substring(t.length() - 4));
    }

    private boolean isInvalidTokenError(Throwable err) {
        if (err instanceof HEADFcmSendException fe) {
            if ("UNREGISTERED".equals(fe.errorCode())) return true;
            return "INVALID_ARGUMENT".equals(fe.status()) || "NOT_FOUND".equals(fe.status());
        }
        return false;
    }
}
