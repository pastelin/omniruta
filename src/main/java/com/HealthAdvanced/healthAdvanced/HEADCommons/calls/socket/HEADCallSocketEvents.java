package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket;

public final class HEADCallSocketEvents {
    private HEADCallSocketEvents() {}

    public static final String CALL_START = "CALL_START";
    public static final String CALL_END           = "CALL_END";

    public static final String CALL_SDP           = "CALL_SDP";           // OFFER/ANSWER
    public static final String CALL_ICE_CANDIDATE  = "CALL_ICE_CANDIDATE";

    public static final String CALL_STATE_UPDATE  = "CALL_STATE_UPDATE";
}
