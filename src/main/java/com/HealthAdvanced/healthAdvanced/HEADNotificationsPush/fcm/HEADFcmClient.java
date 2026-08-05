package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.fcm;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.configs.HEADFcmProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.exceptions.HEADFcmSendException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model.HEADFcmAndroidOptions;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.request.clientFcm.FcmV1Request;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class HEADFcmClient {

    private final WebClient webClient;
    private final HEADFcmProperties props;
    private final ObjectMapper om = new ObjectMapper();

    private final AtomicReference<CachedToken> cached = new AtomicReference<>(null);

    public HEADFcmClient(WebClient.Builder builder, HEADFcmProperties props) {
        this.webClient = builder.baseUrl("https://fcm.googleapis.com").build();
        this.props = props;
    }

    public Mono<Void> sendToToken(String token,
                                  String title,
                                  String body,
                                  Map<String, String> data,
                                  HEADFcmAndroidOptions options) {

        var opt = (options != null ? options : HEADFcmAndroidOptions.defaults());

        String ttl = opt.ttlSeconds() != null ? (opt.ttlSeconds() + "s") : null;

        var android = new FcmV1Request.Android(
                opt.priority() != null ? opt.priority() : "HIGH",
                ttl,
                opt.collapseKey(),
                new FcmV1Request.AndroidNotification(
                        opt.channelId() != null ? opt.channelId() : "head_push",
                        opt.tag(),
                        "default"
                )
        );

        return obtainGoogleAccessTokenMono()
                .flatMap(accessToken -> {

                    var req = new FcmV1Request(
                            new FcmV1Request.Message(
                                    token,
                                    new FcmV1Request.Notification(title, body),
                                    data,
                                    android
                            )
                    );

                    return webClient.post()
                            .uri("/v1/projects/" + props.projectId() + "/messages:send")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(req)
                            .retrieve()
                            .bodyToMono(String.class)
                            .then();
                })
                .onErrorMap(this::mapFcmError);
    }

    // ===== token cache =====

    private Mono<String> obtainGoogleAccessTokenMono() {
        return Mono.fromCallable(this::obtainGoogleAccessTokenCached);
    }

    private String obtainGoogleAccessTokenCached() {
        var curr = cached.get();
        var now = Instant.now();

        if (curr != null && curr.expiresAt().isAfter(now.plusSeconds(60))) {
            return curr.token();
        }

        try {
            GoogleCredentials cred = GoogleCredentials
                    .fromStream(props.serviceAccount().getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));

            cred.refreshIfExpired();
            var at = cred.getAccessToken();

            Instant expiresAt = at.getExpirationTime() != null
                    ? at.getExpirationTime().toInstant()
                    : now.plusSeconds(50 * 60);

            cached.set(new CachedToken(at.getTokenValue(), expiresAt));
            return at.getTokenValue();

        } catch (IOException e) {
            throw new HEADBusinessException("Error loading FCM credentials: " + e.getMessage());
        }
    }

    private record CachedToken(String token, Instant expiresAt) {}

    // ===== error mapping =====

    private Throwable mapFcmError(Throwable err) {
        if (!(err instanceof WebClientResponseException ex)) return err;

        String body = ex.getResponseBodyAsString();
        try {
            JsonNode root = om.readTree(body);
            JsonNode error = root.path("error");
            String status = error.path("status").asText(null);
            String message = error.path("message").asText(ex.getMessage());

            String errorCode = null;
            for (JsonNode d : error.path("details")) {
                if ("UNREGISTERED".equals(d.path("errorCode").asText(null))) {
                    errorCode = "UNREGISTERED";
                    break;
                }
                if (errorCode == null) errorCode = d.path("errorCode").asText(null);
            }

            return new HEADFcmSendException(
                    ex.getRawStatusCode(),
                    status,
                    errorCode,
                    message,
                    ex
            );
        } catch (Exception parseFail) {
            return ex;
        }
    }
}
