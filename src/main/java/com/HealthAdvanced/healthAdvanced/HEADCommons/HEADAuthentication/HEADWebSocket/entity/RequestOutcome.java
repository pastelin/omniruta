package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.entity;

import lombok.Getter;

import java.util.Map;

@Getter
public class RequestOutcome {
    private final boolean success;
    private final int errorCode;
    private final String errorMessage;
    private final String targetPersonalUuid;
    private final Object clientPayload;
    private final Object personalPayload;

    private RequestOutcome(boolean success, int errorCode, String errorMessage,
                           String targetPersonalUuid, Object clientPayload, Object personalPayload) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.targetPersonalUuid = targetPersonalUuid;
        this.clientPayload = clientPayload;
        this.personalPayload = personalPayload;
    }

    public static RequestOutcome success(String targetPersonalUuid, Object clientPayload, Object personalPayload) {
        return new RequestOutcome(true, 0, null, targetPersonalUuid, clientPayload, personalPayload);
    }

    public static RequestOutcome errorForClient(int code, String message) {
        return new RequestOutcome(false, code, message, null, Map.of("code", code, "message", message), null);
    }

}
