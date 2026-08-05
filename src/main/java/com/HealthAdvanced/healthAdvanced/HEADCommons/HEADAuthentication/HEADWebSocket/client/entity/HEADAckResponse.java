package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity;

public record HEADAckResponse(boolean ok, String error) {
    public static HEADAckResponse oks() { return new HEADAckResponse(true, null); }
    public static HEADAckResponse fail(String msg) { return new HEADAckResponse(false, msg); }
}
