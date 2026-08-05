package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// WsEnvelope.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HEADWsEnvelope<T> {
    private boolean ok;
    private String  message;
    private String  v;
    private long    ts;
    private T result;

    public static <T> HEADWsEnvelope<T> ok(String msg, T data) {
        return new HEADWsEnvelope<>(true, msg, "1.0", System.currentTimeMillis(), data);
    }
    public static <T> HEADWsEnvelope<T> fail(String msg) {
        return new HEADWsEnvelope<>(false, msg, "1.0", System.currentTimeMillis(), null);
    }
}

